#!/usr/bin/env python3
"""
reproduce_issues.py — black-box reproduction harness for Aerofare.

Drives the running REST API and prints OBSERVED vs EXPECTED for each open
production ticket, then checks the acceptance criteria for the new refund
feature. It does NOT inspect the source or explain root causes — it just gives
you a fast, repeatable red/green signal.

Usage:
    1. Build and start the service:
           mvn clean package
           java -jar aerofare-app/target/aerofare-app-1.0.0.jar
    2. Run this script:
           python3 reproduce_issues.py
       (optionally: python3 reproduce_issues.py --base-url http://localhost:8080)

Requires only the Python standard library (Python 3.8+).
"""

import argparse
import json
import sys
import urllib.error
import urllib.request

BASE_URL = "http://localhost:8080"

# Known seed flight ids (see data.sql).
FLIGHT_ID = {"FL100": 1, "FL200": 2, "FL300": 3, "FL400": 4, "FL500": 5}


# --------------------------------------------------------------------------- #
# HTTP helper (stdlib only)
# --------------------------------------------------------------------------- #
def request(method, path, body=None):
    url = BASE_URL + path
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url=url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    req.add_header("Accept", "application/json")
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            return resp.status, _parse(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return e.code, _parse(e.read().decode("utf-8"))
    except urllib.error.URLError as e:
        print(f"\n[!] Could not reach {url}: {e.reason}")
        print("    Is the service running?  ->  java -jar aerofare-app/target/aerofare-app-1.0.0.jar")
        sys.exit(2)


def _parse(raw):
    if not raw:
        return None
    try:
        return json.loads(raw)
    except json.JSONDecodeError:
        return raw


# --------------------------------------------------------------------------- #
# API wrappers
# --------------------------------------------------------------------------- #
def search(origin, destination, date, cabin="ECONOMY", passengers=1):
    status, body = request(
        "GET",
        f"/api/flights/search?origin={origin}&destination={destination}"
        f"&date={date}&cabin={cabin}&passengers={passengers}",
    )
    return status, body if isinstance(body, list) else []


def create_booking(flight_number, cabin, passengers, email="repro@example.com"):
    return request("POST", "/api/bookings", {
        "flightNumber": flight_number,
        "cabinClass": cabin,
        "contactEmail": email,
        "passengers": passengers,
    })


def get_booking(pnr):
    return request("GET", f"/api/bookings/{pnr}")


def refund(pnr):
    return request("POST", f"/api/bookings/{pnr}/refund", {})


def inventory(flight_id, cabin):
    status, body = request("GET", f"/api/admin/inventory?flightId={flight_id}&cabin={cabin}")
    if isinstance(body, dict):
        return body.get("availableSeats")
    return None


def pax(first, last, ptype="ADULT", dob="1990-01-01"):
    return {"firstName": first, "lastName": last, "dateOfBirth": dob, "passengerType": ptype}


# --------------------------------------------------------------------------- #
# Output
# --------------------------------------------------------------------------- #
PASS = "\033[92mOK (as expected)\033[0m"
BUG = "\033[91m🐛 BUG REPRODUCED\033[0m"
MET = "\033[92mMET\033[0m"
NOT_MET = "\033[91mNOT MET\033[0m"


def verdict(reproduced):
    return BUG if reproduced else PASS


def req_verdict(met):
    return MET if met else NOT_MET


def header(ticket, title):
    print("\n" + "=" * 76)
    print(f"  {ticket} — {title}")
    print("=" * 76)


def safe(fn):
    try:
        return fn()
    except Exception as e:  # noqa: BLE001
        print(f"  (check raised {type(e).__name__}: {e})")
        return False


# --------------------------------------------------------------------------- #
# Production ticket reproductions
# --------------------------------------------------------------------------- #
def repro_4102():
    header("AERO-4102", "International flight durations wrong / negative")
    _, results = search("HND", "LAX", "2026-08-01", "ECONOMY", 1)
    fl400 = next((f for f in results if f["flightNumber"] == "FL400"), None)
    dur = fl400["durationMinutes"] if fl400 else None
    print(f"  FL400 HND->LAX durationMinutes = {dur}")
    print(f"  EXPECTED: a positive, realistic block time (~540 min).")
    reproduced = dur is None or dur <= 0
    print(f"  RESULT:   {verdict(reproduced)}")
    return reproduced


def repro_4118():
    header("AERO-4118", "Flights missing from search results")
    _, results = search("JFK", "LAX", "2026-08-01", "ECONOMY", 1)
    numbers = [f["flightNumber"] for f in results]
    print(f"  Search JFK->LAX ECONOMY pax=1 returned: {numbers}")
    print(f"  EXPECTED: both FL100 and FL200 (FL200 has 1 seat left, enough for 1).")
    reproduced = "FL200" not in numbers
    print(f"  RESULT:   {verdict(reproduced)}")
    return reproduced


def repro_4135():
    header("AERO-4135", "Booking a particular party size 500s")
    party = [pax("Ann", "Lee"), pax("Bob", "Ray"), pax("Cy", "Fox")]
    status, body = create_booking("FL300", "ECONOMY", party)
    print(f"  Booking a party of 3 on FL300: status={status}")
    print(f"  EXPECTED: 201 CREATED.")
    reproduced = status >= 500
    print(f"  RESULT:   {verdict(reproduced)}")
    return reproduced


def repro_4150():
    header("AERO-4150", "A passenger silently goes missing from the PNR")
    party = [pax("John", "Smith", "ADULT", "1980-01-01"),
             pax("John", "Smith", "CHILD", "2015-05-05")]
    status, body = create_booking("FL500", "ECONOMY", party)
    count = body.get("passengerCount") if isinstance(body, dict) else None
    print(f"  Booked two travellers both named 'John Smith': status={status}, passengerCount={count}")
    print(f"  EXPECTED: 2 passengers on the PNR.")
    reproduced = count is not None and count < 2
    print(f"  RESULT:   {verdict(reproduced)}")
    return reproduced


def repro_4177():
    header("AERO-4177", "Premium cabin priced as economy")
    _, eco = search("JFK", "LAX", "2026-08-01", "ECONOMY", 1)
    _, bus = search("JFK", "LAX", "2026-08-01", "BUSINESS", 1)
    eco_price = next((f["priceFrom"] for f in eco if f["flightNumber"] == "FL100"), None)
    bus_price = next((f["priceFrom"] for f in bus if f["flightNumber"] == "FL100"), None)
    print(f"  FL100 ECONOMY priceFrom={eco_price}, then BUSINESS priceFrom={bus_price}")
    print(f"  EXPECTED: business fare noticeably higher than economy.")
    reproduced = eco_price is not None and bus_price is not None and float(bus_price) <= float(eco_price)
    print(f"  RESULT:   {verdict(reproduced)}")
    return reproduced


# --------------------------------------------------------------------------- #
# New requirement — AERO-REQ-210: booking refund
# --------------------------------------------------------------------------- #
def check_refund_requirement():
    header("AERO-REQ-210", "Booking refund (new requirement)")

    def ac_unknown():
        print("  [AC-1] unknown PNR -> 404")
        status, _ = refund("ZZZZZZ")
        print(f"    refund status={status}")
        return status == 404

    def ac_non_refundable():
        print("  [AC-2] non-refundable (ECONOMY) booking -> 409")
        _, booking = create_booking("FL100", "ECONOMY", [pax("Nore", "Fund")])
        pnr = booking.get("recordLocator") if isinstance(booking, dict) else None
        if not pnr:
            print(f"    could not create booking: {booking}")
            return False
        status, body = refund(pnr)
        print(f"    refund {pnr} status={status}")
        return status == 409

    def ac_refundable_math():
        print("  [AC-3] refundable (BUSINESS) booking -> refund = total - 10% cancellation fee")
        _, booking = create_booking("FL200", "BUSINESS", [pax("Rea", "Fundable")])
        pnr = booking.get("recordLocator") if isinstance(booking, dict) else None
        total = booking.get("totalAmount") if isinstance(booking, dict) else None
        if not pnr:
            print(f"    could not create booking: {booking}")
            return False
        status, body = refund(pnr)
        print(f"    booking total={total}; refund status={status} body={body}")
        if status != 200 or not isinstance(body, dict) or total is None:
            return False
        expected = round(float(total) * 0.90, 2)
        return abs(float(body.get("refundedAmount", -1)) - expected) < 0.01 \
            and body.get("status") == "REFUNDED"

    def ac_release_seats():
        print("  [AC-4] refund releases the seats back to inventory")
        before = inventory(FLIGHT_ID["FL400"], "BUSINESS")
        _, booking = create_booking("FL400", "BUSINESS", [pax("See", "Trelease")])
        pnr = booking.get("recordLocator") if isinstance(booking, dict) else None
        if not pnr:
            print(f"    could not create booking: {booking}")
            return False
        refund(pnr)
        after = inventory(FLIGHT_ID["FL400"], "BUSINESS")
        print(f"    BUSINESS availableSeats before={before}, after refund={after}")
        return before is not None and after is not None and after == before

    def ac_already_refunded():
        print("  [AC-5] refunding an already-refunded booking -> 409 (idempotent, no double refund)")
        _, booking = create_booking("FL500", "BUSINESS", [pax("Al", "Ready")])
        pnr = booking.get("recordLocator") if isinstance(booking, dict) else None
        if not pnr:
            print(f"    could not create booking: {booking}")
            return False
        first_status, _ = refund(pnr)
        second_status, second_body = refund(pnr)
        booking_status, booking_view = request("GET", f"/api/bookings/{pnr}")
        now_status = booking_view.get("status") if isinstance(booking_view, dict) else None
        print(f"    1st refund={first_status}, 2nd refund={second_status}, booking status now={now_status}")
        print(f"    EXPECTED: 1st=200, 2nd=409, booking marked REFUNDED.")
        return first_status == 200 and second_status == 409 and now_status == "REFUNDED"

    checks = {
        "AC-1 unknown PNR 404": safe(ac_unknown),
        "AC-2 non-refundable 409": safe(ac_non_refundable),
        "AC-3 refund math + status": safe(ac_refundable_math),
        "AC-4 seats released": safe(ac_release_seats),
        "AC-5 already-refunded 409": safe(ac_already_refunded),
    }
    print("\n  Criteria:")
    for name, met in checks.items():
        print(f"    {name:<28}: {req_verdict(met)}")

    met_count = sum(1 for v in checks.values() if v)
    all_met = met_count == len(checks)
    print(f"\n  RESULT:   {met_count}/{len(checks)} criteria met -> "
          + ("\033[92mFEATURE COMPLETE\033[0m" if all_met else "\033[91mNOT IMPLEMENTED YET\033[0m"))
    return all_met


# --------------------------------------------------------------------------- #
# Main
# --------------------------------------------------------------------------- #
def main():
    global BASE_URL
    parser = argparse.ArgumentParser(description="Reproduce Aerofare production tickets.")
    parser.add_argument("--base-url", default=BASE_URL, help="Service base URL")
    args = parser.parse_args()
    BASE_URL = args.base_url.rstrip("/")

    print(f"Target: {BASE_URL}")
    status, _ = search("JFK", "LAX", "2026-08-01")
    print(f"Health check GET /api/flights/search -> {status}")

    results = {
        "AERO-4102": repro_4102(),
        "AERO-4118": repro_4118(),
        "AERO-4135": repro_4135(),
        "AERO-4150": repro_4150(),
        "AERO-4177": repro_4177(),
    }
    requirement_met = check_refund_requirement()

    print("\n" + "=" * 76)
    print("  SUMMARY")
    print("=" * 76)
    print("  Production defects:")
    for ticket, res in results.items():
        print(f"    {ticket}: {BUG if res else PASS}")
    print("  New requirement:")
    print(f"    AERO-REQ-210 (refund): "
          + ("\033[92mFEATURE COMPLETE\033[0m" if requirement_met else "\033[91mNOT IMPLEMENTED YET\033[0m"))
    print()


if __name__ == "__main__":
    main()
