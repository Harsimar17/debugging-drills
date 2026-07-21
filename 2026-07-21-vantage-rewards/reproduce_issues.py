#!/usr/bin/env python3
"""
reproduce_issues.py — black-box reproduction harness for Vantage Rewards.

Drives the running REST API and prints OBSERVED vs EXPECTED for each open
production ticket. It does NOT inspect the source or tell you *why* anything
happens — it just gives you a fast, repeatable way to make each symptom appear.

Usage:
    1. Start the service in one terminal:
           mvn spring-boot:run
    2. Run this script in another:
           python3 reproduce_issues.py
       (optionally: python3 reproduce_issues.py --base-url http://localhost:8080)

Requires only the Python standard library (Python 3.8+).
"""

import argparse
import json
import sys
import time
import urllib.error
import urllib.request

BASE_URL = "http://localhost:8080"


# --------------------------------------------------------------------------- #
# Tiny HTTP helper (stdlib only)
# --------------------------------------------------------------------------- #
def request(method, path, body=None):
    """Return (status_code, parsed_body). Never raises on HTTP error status."""
    url = BASE_URL + path
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url=url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    req.add_header("Accept", "application/json")
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            raw = resp.read().decode("utf-8")
            return resp.status, _parse(raw)
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8")
        return e.code, _parse(raw)
    except urllib.error.URLError as e:
        print(f"\n[!] Could not reach {url}: {e.reason}")
        print("    Is the service running?  ->  mvn spring-boot:run")
        sys.exit(2)


def _parse(raw):
    if not raw:
        return None
    try:
        return json.loads(raw)
    except json.JSONDecodeError:
        return raw


def unique_ref(prefix="ORDER"):
    return f"{prefix}-{int(time.time() * 1000)}-{_next_seq()}"


_SEQ = 0


def _next_seq():
    global _SEQ
    _SEQ += 1
    return _SEQ


# --------------------------------------------------------------------------- #
# API convenience wrappers
# --------------------------------------------------------------------------- #
def enroll(tier, name="Repro Tester"):
    status, body = request("POST", "/api/members", {
        "fullName": name,
        "email": f"repro{_next_seq()}@example.com",
        "tier": tier,
    })
    if status != 201:
        raise RuntimeError(f"enroll({tier}) failed: {status} {body}")
    return body["id"]


def earn(member_id, amount, ref=None, description="Repro spend"):
    ref = ref or unique_ref()
    return request("POST", f"/api/members/{member_id}/points/earn", {
        "spendAmount": amount,
        "sourceReference": ref,
        "description": description,
    })


def redeem(member_id, sku):
    return request("POST", f"/api/members/{member_id}/points/redeem", {"sku": sku})


def summary(member_id):
    return request("GET", f"/api/members/{member_id}/account/summary")


def enroll_full(tier, name="Repro Tester"):
    """Enroll and return the full member dict (id + memberNumber)."""
    status, body = request("POST", "/api/members", {
        "fullName": name,
        "email": f"repro{_next_seq()}@example.com",
        "tier": tier,
    })
    if status != 201:
        raise RuntimeError(f"enroll({tier}) failed: {status} {body}")
    return body


def transfer(from_member_id, to_member_number, points, note="Gift"):
    return request("POST", f"/api/members/{from_member_id}/transfers", {
        "toMemberNumber": to_member_number,
        "points": points,
        "note": note,
    })


def ledger(member_id):
    status, body = request("GET", f"/api/members/{member_id}/account/ledger?size=200")
    if isinstance(body, dict) and "content" in body:
        return body["content"]
    return []


def adjustments(member_id):
    return [e for e in ledger(member_id) if e.get("entryType") == "ADJUSTMENT"]


# --------------------------------------------------------------------------- #
# Pretty output
# --------------------------------------------------------------------------- #
PASS = "\033[92mOK (as expected)\033[0m"
BUG = "\033[91m🐛 BUG REPRODUCED\033[0m"

MET = "\033[92mMET\033[0m"
NOT_MET = "\033[91mNOT MET\033[0m"
MANUAL = "\033[93mMANUAL\033[0m"


def verdict(reproduced):
    return BUG if reproduced else PASS


def req_verdict(met):
    if met is None:
        return MANUAL
    return MET if met else NOT_MET


def safe(fn):
    """Run a criterion check, converting any unexpected error into NOT MET."""
    try:
        return fn()
    except Exception as e:  # noqa: BLE001 - report, don't crash the suite
        print(f"  (check raised {type(e).__name__}: {e})")
        return False


def header(ticket, title):
    print("\n" + "=" * 74)
    print(f"  {ticket} — {title}")
    print("=" * 74)


# --------------------------------------------------------------------------- #
# Reproductions (symptom-level only)
# --------------------------------------------------------------------------- #
def repro_611():
    header("LOYAL-611", "Higher-tier members occasionally miss their bonus points")
    member = enroll("GOLD", "Gold Member")
    status, body = earn(member, 100.00)
    entries = body if isinstance(body, list) else []
    total = sum(e.get("points", 0) for e in entries)
    print(f"  Enrolled GOLD member id={member}, earned on a $100.00 spend.")
    print(f"  EXPECTED: 2 ledger entries (base 100 + GOLD bonus 50 = 150 points).")
    print(f"  OBSERVED: {len(entries)} entrie(s), total {total} points -> {entries!r}")
    reproduced = len(entries) < 2 or total < 150
    print(f"  RESULT:   {verdict(reproduced)}")
    return reproduced


def repro_627():
    header("LOYAL-627", "Balance does not drop after redemption")
    member = enroll("STANDARD", "Standard Member")
    earn(member, 300.00)  # -> 300 base points, balance should be 300
    status, body = redeem(member, "COFFEE-01")  # costs 150 points
    remaining = body.get("remainingBalance") if isinstance(body, dict) else None
    print(f"  Enrolled STANDARD member id={member}, earned 300, redeemed COFFEE-01 (150).")
    print(f"  EXPECTED: remaining balance = 150.")
    print(f"  OBSERVED: redeem status={status}, remainingBalance={remaining}")
    _, summ = summary(member)
    if isinstance(summ, dict):
        print(f"            summary availablePoints={summ.get('availablePoints')}")
    reproduced = remaining != 150
    print(f"  RESULT:   {verdict(reproduced)}")
    return reproduced


def repro_644():
    header("LOYAL-644", "Account summary intermittently returns 500 for some members")
    # Control: a balance that is a clean multiple works fine.
    ok_member = enroll("STANDARD", "Summary OK")
    earn(ok_member, 300.00)
    ok_status, ok_body = summary(ok_member)

    # Trigger: a different balance makes the same endpoint 500.
    bad_member = enroll("STANDARD", "Summary Boom")
    earn(bad_member, 100.00)
    bad_status, bad_body = summary(bad_member)

    print(f"  Member id={ok_member} (balance 300): summary status={ok_status} "
          f"cashValue={ok_body.get('estimatedCashValue') if isinstance(ok_body, dict) else ok_body}")
    print(f"  Member id={bad_member} (balance 100): summary status={bad_status} body={bad_body}")
    print(f"  EXPECTED: both return 200 with a cash value.")
    print(f"  OBSERVED: one member 500s while the other succeeds.")
    reproduced = bad_status >= 500 and ok_status == 200
    print(f"  RESULT:   {verdict(reproduced)}")
    return reproduced


def repro_702():
    header("LOYAL-702", "Earning crashes with 500 for Platinum members only")
    plat = enroll("PLATINUM", "Platinum Member")
    status, body = earn(plat, 100.00)
    print(f"  Enrolled PLATINUM member id={plat}, attempted to earn on a $100.00 spend.")
    print(f"  EXPECTED: 200/201 with credited points.")
    print(f"  OBSERVED: status={status} body={body}")
    reproduced = status >= 500
    print(f"  RESULT:   {verdict(reproduced)}")
    return reproduced


def repro_659():
    header("LOYAL-659", "Points expire a day early near month boundaries (region-dependent)")
    print("  This one is timing/timezone-sensitive and cannot be forced through the")
    print("  HTTP API alone, because it depends on the JVM's clock/zone at the moment")
    print("  points are written versus when the nightly sweep evaluates them.")
    print()
    print("  To reproduce deterministically, run the service (or the relevant unit")
    print("  under test) pinned to a non-UTC zone and with an entry expiring close to")
    print("  midnight, e.g.:")
    print("      mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Duser.timezone=America/Los_Angeles'")
    print("  then compare the statement's stated expiry date against when the sweep")
    print("  (POST /api/admin/expiry/run) actually retires the points.")
    print(f"  RESULT:   \033[93mMANUAL — see steps above\033[0m")
    return None


# --------------------------------------------------------------------------- #
# New requirement — VANTAGE-REQ-118: peer-to-peer points transfer
# (acceptance checks; every criterion is NOT MET until the feature is built)
# --------------------------------------------------------------------------- #
def check_transfer_requirement():
    header("VANTAGE-REQ-118", "Peer-to-peer points transfer (new requirement)")

    def ac_debit_credit():
        print("  [AC-2/3] atomic debit + credit as ADJUSTMENT entries")
        sender = enroll_full("STANDARD", "Sender A")
        recipient = enroll_full("STANDARD", "Recipient A")
        earn(sender["id"], 1000.00)
        status, body = transfer(sender["id"], recipient["memberNumber"], 400)
        s_adj, r_adj = adjustments(sender["id"]), adjustments(recipient["id"])
        print(f"    transfer status={status}; sender ADJ={s_adj}; recipient ADJ={r_adj}")
        return (200 <= status < 300
                and any(e.get("points") == -400 for e in s_adj)
                and any(e.get("points") == 400 for e in r_adj))

    def ac_reference():
        print("  [AC-3] each entry references the counterparty")
        sender = enroll_full("STANDARD", "Sender B")
        recipient = enroll_full("STANDARD", "Recipient B")
        earn(sender["id"], 800.00)
        status, _ = transfer(sender["id"], recipient["memberNumber"], 250)
        s_adj, r_adj = adjustments(sender["id"]), adjustments(recipient["id"])
        if not s_adj or not r_adj:
            print(f"    no ADJUSTMENT entries produced (status={status})")
            return False
        s_note, r_note = s_adj[0].get("description") or "", r_adj[0].get("description") or ""
        print(f"    sender note={s_note!r}; recipient note={r_note!r}")
        return (recipient["memberNumber"] in s_note or recipient["fullName"] in s_note) and \
               (sender["memberNumber"] in r_note or sender["fullName"] in r_note)

    def ac_inherit_validity():
        print("  [AC-4] transferred points inherit sender's remaining validity")
        sender = enroll_full("STANDARD", "Sender C")
        recipient = enroll_full("STANDARD", "Recipient C")
        _, earned = earn(sender["id"], 500.00)
        sender_expiry = earned[0].get("expiresAt") if isinstance(earned, list) and earned else None
        status, _ = transfer(sender["id"], recipient["memberNumber"], 500)
        r_adj = adjustments(recipient["id"])
        credited_expiry = r_adj[0].get("expiresAt") if r_adj else None
        print(f"    sender EARN expiry={sender_expiry}; credited expiry={credited_expiry} (status={status})")
        return credited_expiry is not None and credited_expiry == sender_expiry

    def ac_insufficient():
        print("  [AC-1] reject insufficient points, no side effects")
        sender = enroll_full("STANDARD", "Sender D")
        recipient = enroll_full("STANDARD", "Recipient D")
        earn(sender["id"], 100.00)
        status, body = transfer(sender["id"], recipient["memberNumber"], 500)
        print(f"    transfer status={status} body={body}")
        return status == 409 and not adjustments(sender["id"]) and not adjustments(recipient["id"])

    def ac_unknown_recipient():
        print("  [AC-5] reject unknown recipient")
        sender = enroll_full("STANDARD", "Sender E")
        earn(sender["id"], 1000.00)
        status, body = transfer(sender["id"], "VG000000", 100)
        print(f"    transfer to VG000000 status={status} body={body}")
        return status == 404

    def ac_self_transfer():
        print("  [AC-5] reject self-transfer")
        sender = enroll_full("STANDARD", "Sender F")
        earn(sender["id"], 1000.00)
        status, body = transfer(sender["id"], sender["memberNumber"], 100)
        print(f"    transfer to self status={status} body={body}")
        return 400 <= status < 500 and not adjustments(sender["id"])

    checks = {
        "AC-1 insufficient points": safe(ac_insufficient),
        "AC-2/3 debit + credit":    safe(ac_debit_credit),
        "AC-3 counterparty ref":    safe(ac_reference),
        "AC-4 validity inherited":  safe(ac_inherit_validity),
        "AC-5 unknown recipient":   safe(ac_unknown_recipient),
        "AC-5 self-transfer":       safe(ac_self_transfer),
    }
    print("\n  Criteria:")
    for name, met in checks.items():
        print(f"    {name:<26}: {req_verdict(met)}")
    print("    AC-5 inactive recipient   : " + req_verdict(None)
          + " (no API to suspend a member; see JUnit TransferServiceTest)")

    testable = list(checks.values())
    met_count = sum(1 for v in testable if v)
    all_met = met_count == len(testable)
    print(f"\n  RESULT:   {met_count}/{len(testable)} criteria met -> "
          + ("\033[92mFEATURE COMPLETE\033[0m" if all_met else "\033[91mNOT IMPLEMENTED YET\033[0m"))
    return all_met


# --------------------------------------------------------------------------- #
# Main
# --------------------------------------------------------------------------- #
def main():
    global BASE_URL
    parser = argparse.ArgumentParser(description="Reproduce Vantage Rewards production tickets.")
    parser.add_argument("--base-url", default=BASE_URL, help="Service base URL")
    args = parser.parse_args()
    BASE_URL = args.base_url.rstrip("/")

    print(f"Target: {BASE_URL}")
    status, _ = request("GET", "/api/catalog")
    print(f"Health check GET /api/catalog -> {status}")

    results = {
        "LOYAL-611": repro_611(),
        "LOYAL-627": repro_627(),
        "LOYAL-644": repro_644(),
        "LOYAL-702": repro_702(),
        "LOYAL-659": repro_659(),
    }

    requirement_met = check_transfer_requirement()

    print("\n" + "=" * 74)
    print("  SUMMARY")
    print("=" * 74)
    print("  Production defects:")
    for ticket, res in results.items():
        if res is None:
            label = "\033[93mMANUAL\033[0m"
        else:
            label = BUG if res else PASS
        print(f"    {ticket}: {label}")
    print("  New requirement:")
    print(f"    VANTAGE-REQ-118 (points transfer): "
          + ("\033[92mFEATURE COMPLETE\033[0m" if requirement_met else "\033[91mNOT IMPLEMENTED YET\033[0m"))
    print()


if __name__ == "__main__":
    main()
