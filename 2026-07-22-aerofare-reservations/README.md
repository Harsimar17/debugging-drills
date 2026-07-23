# Aerofare — Airline Reservations & Fare Engine

Aerofare is the reservations platform for a mid-size carrier. It lets customers
search flights on a route and date, get a priced quote for a party in a chosen
cabin, create a booking (PNR), and have tickets issued. Seat inventory is
adjusted as bookings confirm, and background jobs keep the schedule tidy.

You have just inherited this service to support. Several production incidents are
open (see **Known Production Issues**). Your job is to investigate, reproduce and
fix them — and then build one new capability.

---

## Business Requirements

1. **Flight search** — Given an origin, destination, date, cabin and party size,
   return the flights that operate the route that day and still have enough seats
   in the requested cabin, each with an indicative "from" price and the flight's
   duration.
2. **Pricing** — A party's fare is the sum of each passenger's fare (the flight
   base fare × the fare-rule multiplier for their cabin and passenger type), plus
   a long-haul surcharge on long sectors, plus tax, plus a flat per-booking
   service fee shared across the party. All money is held to 2 decimal places.
3. **Booking** — Create a PNR for a party on a flight/cabin. A booking confirms
   seat inventory for its cabin. The same real passenger must not appear twice on
   one PNR.
4. **Ticketing** — Issue one ticket per passenger on a confirmed booking, then
   mark the booking ticketed.
5. **Duration** — A flight's elapsed flying time is derived from its scheduled
   departure and arrival, each of which is expressed in its own airport's local
   time zone.
6. **Housekeeping** — A scheduled job expires bookings left on hold past their
   window and releases their seats.

---

## Architecture Overview

Multi-module Maven project, layered by module:

```
aerofare-common       enums, exceptions, shared utilities, domain events
aerofare-domain       JPA entities
aerofare-repository   Spring Data JPA repositories
aerofare-service      business logic: search, pricing, booking, ticketing,
                      refunds, seat inventory, event listeners, mappers
aerofare-app          Spring Boot application: REST controllers, config,
                      scheduled batch jobs, exception handling, seed data
```

Notable design points:

- **Event-driven seat inventory** — confirming a booking publishes a
  `BookingConfirmedEvent`; a listener decrements seat inventory in the same
  transaction, so a shortfall rolls the booking back.
- **In-process fare cache** — recently priced flights are cached to avoid
  recomputing during a search-then-book flow.
- **Scheduled batch** — hold-expiry and a flight-status heartbeat.

Stack: **Java 17**, **Spring Boot 3.2**, **Spring MVC**, **Spring Data JPA /
Hibernate**, **H2** (in-memory, seeded from `data.sql`), **JUnit 5**. No Lombok.

---

## How to Build

```bash
mvn clean package
```

This builds all five modules and produces the runnable jar at
`aerofare-app/target/aerofare-app-1.0.0.jar`.

## How to Run

```bash
java -jar aerofare-app/target/aerofare-app-1.0.0.jar
# or
mvn -pl aerofare-app spring-boot:run
```

The service starts on `http://localhost:8080`. The H2 console is at
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:aerofare`, user `sa`, no
password). Seed data loads six airports, two aircraft, five flights (`FL100`,
`FL200`, `FL300`, `FL400`, `FL500`), their seat inventory and a fare-rule table.

---

## API Documentation

| Method | Path | Description |
|--------|------|-------------|
| GET  | `/api/flights/search?origin=&destination=&date=&cabin=&passengers=` | Search flights |
| POST | `/api/bookings` | Create a booking (PNR) |
| GET  | `/api/bookings/{recordLocator}` | Fetch a booking |
| POST | `/api/bookings/{recordLocator}/tickets` | Issue tickets |
| POST | `/api/bookings/{recordLocator}/refund` | Refund a booking (TBD) |
| GET  | `/api/admin/inventory?flightId=&cabin=` | Seat availability |

### Example — search

```bash
curl "http://localhost:8080/api/flights/search?origin=JFK&destination=LAX&date=2026-08-01&cabin=ECONOMY&passengers=1"
```

### Example — create a booking

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H 'Content-Type: application/json' \
  -d '{
        "flightNumber": "FL100",
        "cabinClass": "ECONOMY",
        "contactEmail": "traveller@example.com",
        "passengers": [
          {"firstName": "Ada", "lastName": "Byron", "dateOfBirth": "1985-12-10", "passengerType": "ADULT"}
        ]
      }'
```

### Example — issue tickets

```bash
curl -X POST http://localhost:8080/api/bookings/ABC123/tickets
```

---

## Expected Behaviour

- Search returns every flight on the route/date with enough seats in the cabin,
  priced correctly for the requested cabin.
- A flight's duration reflects real elapsed time across time zones.
- Booking prices the exact party submitted, keeps every distinct passenger, and
  reserves the right number of seats.
- Ticketing issues one ticket per passenger.

---

## Known Production Issues (reported by QA / customers)

Open tickets against this service. Symptoms only — root causes are under
investigation.

- **AERO-4102** — "International flight durations are wrong. Some long-haul
  flights even display a *negative* duration."
- **AERO-4118** — "Certain searches return fewer flights than expected. Flights
  we know operate that day, and that still have seats, sometimes don't appear in
  the results at all."
- **AERO-4135** — "Creating a booking for a party of a particular size fails with
  a 500. Smaller parties — and some larger ones — work fine."
- **AERO-4150** — "Occasionally a confirmed booking ends up with fewer passengers
  than were submitted. A traveller silently goes missing from the PNR."
- **AERO-4177** — "Fares are sometimes wrong. A premium cabin occasionally gets
  priced as though it were economy, seemingly depending on what was searched just
  before."

> There are **five** distinct defects behind these tickets. They are independent
> of one another and span the search, pricing, booking and domain layers.

---

## Feature to Build (new requirement)

**AERO-REQ-210: Booking refund**

Customers must be able to cancel a booking and be refunded, net of a
cancellation fee.

`POST /api/bookings/{recordLocator}/refund`

Acceptance criteria:

1. Only a `CONFIRMED` or `TICKETED` booking may be refunded; any other status is
   rejected (`409`).
2. If the booking's fare (its cabin's `ADULT` fare rule) is **not refundable**,
   reject the request (`409`) — non-refundable fares cannot be cancelled for a
   cash refund.
3. Otherwise compute the **cancellation fee** as `cancellationFeePercent` of the
   booking total, and the **refund** as `total − fee`, both to 2 decimal places.
4. Record a `Payment` of type/refund reflecting the refunded amount, set the
   booking status to `REFUNDED`, mark any issued tickets `REFUNDED`, and
   **release the seats** back to inventory for the booked cabin.
5. The whole operation must be atomic — a failure at any step leaves the booking
   unchanged.
6. Return an `AERO`-style result payload: record locator, original amount,
   cancellation fee, refunded amount, and the new status. Add tests.

A `RefundService` skeleton and the endpoint already exist and return
`501 NOT IMPLEMENTED`. Implement the service.

---

## Your Task

1. Reproduce each of the five reported issues.
2. Fix them with production-quality changes and tests.
3. Implement `AERO-REQ-210`.
4. When you're done, say **"I have fixed it."** and I'll review your work.

See `hints.md` for investigative nudges (no locations, no solutions). A
`reproduce_issues.py` harness is included to give you a fast red/green signal.
