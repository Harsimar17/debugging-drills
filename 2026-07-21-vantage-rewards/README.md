# Vantage Rewards — Loyalty & Points Platform

Vantage Rewards is the loyalty engine behind a mid-market retail group. Members
earn points on qualifying spend, points accrue faster at higher membership tiers,
and members redeem points against a rewards catalog. Points have a validity
window and expire if unused. A nightly job sweeps expired points.

You have just been handed this service to support. Several production incidents
have been reported by QA and customers (see **Known Production Issues** below).
Your job is to investigate, reproduce, and fix them.

---

## Business Requirements

1. **Membership** — Customers enroll and are assigned a unique member number and a
   membership tier (`STANDARD`, `SILVER`, `GOLD`, `PLATINUM`).
2. **Earning** — When a member spends money, they earn points equal to the spend
   amount × base earn rate × their tier multiplier. Earning is **idempotent** per
   business reference (an order must never be credited twice). Higher tiers earn a
   bonus on top of the base points.
3. **Balance** — A member's available balance is the signed sum of their active,
   unexpired ledger entries (earnings add, redemptions and expiries subtract).
4. **Redemption** — Members redeem points against catalog items priced in points.
   A redemption is rejected if the member lacks sufficient available points. The
   platform also reports an estimated cash value of a points balance using the
   configured conversion rate.
5. **Expiry** — Earned points are valid for a configurable number of months. A
   nightly sweep marks elapsed points as expired and posts a compensating ledger
   entry so balances stay correct.
6. **Statements** — Members can view an account summary (balance + cash value) and
   a paginated ledger of their transactions.

---

## Architecture Overview

Spring Boot monolith, layered:

```
api/          REST controllers + global exception handling
service/      business logic (earning, redemption, expiry, accounts)
repository/   Spring Data JPA repositories
domain/       JPA entities + enums
dto/          request/response models
mapper/       entity → DTO mapping (hand-written)
scheduler/    nightly expiry sweep
config/       configuration properties, Jackson setup
```

- **Java 17**, **Spring Boot 3.2**, **Spring MVC**, **Spring Data JPA / Hibernate**
- **H2** in-memory database (auto-created, seeded from `data.sql`)
- **JUnit 5 + Mockito** for tests
- No Lombok — plain Java.

The points **ledger** is the source of truth. Every earn, redeem, adjustment and
expiry is an append-only `PointsLedgerEntry`. Balances are derived by summing the
ledger.

---

## How to Build

```bash
mvn clean package
```

Run the tests only:

```bash
mvn test
```

## How to Run

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`. The H2 console is at
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:vantage`, user `sa`, no
password). Seed data creates four members (ids 1–4) and a small rewards catalog.

---

## API Documentation

| Method | Path                                        | Description                              |
|--------|---------------------------------------------|------------------------------------------|
| POST   | `/api/members`                              | Enroll a new member                      |
| GET    | `/api/members/{memberId}`                   | Fetch a member                           |
| POST   | `/api/members/{memberId}/points/earn`       | Credit points for a spend                |
| POST   | `/api/members/{memberId}/points/redeem`     | Redeem points for a catalog item         |
| GET    | `/api/members/{memberId}/account/summary`   | Balance + estimated cash value           |
| GET    | `/api/members/{memberId}/account/ledger`    | Paginated ledger (`?page=&size=`)        |
| GET    | `/api/catalog`                              | List reward catalog items                |
| POST   | `/api/members/{memberId}/transfers`         | Transfer points to another member (TBD)  |
| POST   | `/api/admin/expiry/run`                     | Manually trigger the expiry sweep        |

### Example — earn points

```bash
curl -X POST http://localhost:8080/api/members/2/points/earn \
  -H 'Content-Type: application/json' \
  -d '{"spendAmount": 100.00, "sourceReference": "ORDER-12345", "description": "Weekly shop"}'
```

### Example — redeem

```bash
curl -X POST http://localhost:8080/api/members/1/points/redeem \
  -H 'Content-Type: application/json' \
  -d '{"sku": "COFFEE-01"}'
```

### Example — account summary

```bash
curl http://localhost:8080/api/members/1/account/summary
```

---

## Expected Behaviour

- Enrolling returns `201` with a generated `memberNumber`.
- Earning credits the correct number of points for the member's tier and is
  idempotent per `sourceReference`.
- A member's summary balance decreases when they redeem and increases when they
  earn, and reflects expiries once the nightly sweep has run.
- Redemptions are rejected with `409 CONFLICT` when the balance is too low.
- Points expire exactly at the end of their validity window.

---

## Known Production Issues (reported by QA / customers)

These are the open tickets against this service. Symptoms only — root causes are
unknown and under investigation.

- **LOYAL-611** — "Members on higher tiers occasionally don't receive their tier
  bonus points. The base points are credited, but the extra bonus line is missing
  from the ledger for some earn events."
- **LOYAL-627** — "After redeeming rewards, some members report their available
  balance doesn't go down. A few members appear able to redeem far more than they
  should have."
- **LOYAL-644** — "The account summary endpoint intermittently returns `500` for
  certain members, while it works fine for others."
- **LOYAL-659** — "Customers in some regions say their points expired a day earlier
  than the date shown in their statement, especially around month boundaries."
- **LOYAL-702** — "Earning points crashes with a `500` for our top-tier
  (Platinum) members. Standard, Silver and Gold members are unaffected."

> There are **five** distinct defects behind these tickets. They are independent
> of one another. Some tickets map cleanly to one defect.

---

## Feature to Build (new requirement)

**VANTAGE-REQ-118: Peer-to-peer points transfer**

Product wants members to be able to gift points to another member.

`POST /api/members/{memberId}/transfers`

```json
{ "toMemberNumber": "VG100002", "points": 500, "note": "Happy birthday!" }
```

Acceptance criteria:

1. The sender must have at least `points` available; otherwise return `409`.
2. The transfer must be atomic: debit the sender and credit the recipient in a
   single transaction. A failure on either side must roll back both.
3. The debit and credit must appear in each member's ledger as `ADJUSTMENT`
   entries with a clear description referencing the counterparty.
4. Transferred points inherit the **remaining validity window** of the sender's
   points — they must not have their expiry silently reset to a fresh 12 months.
5. A member cannot transfer points to themselves, and the recipient must exist and
   be `ACTIVE`.
6. Add unit/integration tests covering the happy path and each rejection case.

The endpoint and a `TransferService` skeleton already exist and currently return
`501 NOT IMPLEMENTED`. Implement the service.

---

## Your Task

1. Reproduce each of the five reported issues.
2. Fix them with production-quality changes and tests.
3. Implement `VANTAGE-REQ-118`.
4. When you're done, tell me **"I have fixed it."** and I'll review your work.

See `hints.md` if you get stuck — it points you at areas to investigate without
giving away the answers.
