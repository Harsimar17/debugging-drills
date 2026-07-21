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


# --------------------------------------------------------------------------- #
# Pretty output
# --------------------------------------------------------------------------- #
PASS = "\033[92mOK (as expected)\033[0m"
BUG = "\033[91m🐛 BUG REPRODUCED\033[0m"


def verdict(reproduced):
    return BUG if reproduced else PASS


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

    print("\n" + "=" * 74)
    print("  SUMMARY")
    print("=" * 74)
    for ticket, res in results.items():
        if res is None:
            label = "\033[93mMANUAL\033[0m"
        else:
            label = BUG if res else PASS
        print(f"  {ticket}: {label}")
    print()


if __name__ == "__main__":
    main()
