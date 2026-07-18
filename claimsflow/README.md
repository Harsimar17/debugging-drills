# ClaimsFlow — Insurance Claims Processing Service

ClaimsFlow is the backend service that powers claim intake, adjuster
assignment, and status tracking for a mid-size insurance carrier's
personal lines business (auto, home, health, life, travel).

## Business Requirements

1. Customers (or call-center agents on their behalf) submit a claim against
   an active policy via `POST /api/claims`. The system must generate a
   unique, human-readable **claim reference number** (format
   `CLM-YYYYMMDD-NNNN`) that is communicated back to the customer and used
   by every downstream system — payments, correspondence, audit — to
   identify the claim.
2. Each newly submitted claim is automatically routed to the
   least-loaded adjuster in the relevant region.
3. Claims move through a fixed set of statuses (`SUBMITTED` →
   `UNDER_REVIEW` → `APPROVED`/`REJECTED` → `PAID` → `CLOSED`), and every
   transition is recorded in an audit history table.
4. Claims that sit in `UNDER_REVIEW` for more than 72 hours must be
   automatically escalated (a background sweep runs every 30 minutes).
5. All claim and policy lookups are exposed as REST endpoints for the
   customer portal and the internal case-management UI.

## Architecture Overview

ClaimsFlow is a **Spring Boot monolith** (Java 17, Spring Boot 3.2) with a
classic layered package structure:

```
com.claimsflow
├── domain        JPA entities (Customer, Policy, Claim, ClaimDocument, ClaimStatusHistory, Adjuster)
├── repository    Spring Data JPA repositories
├── dto           Request/response payloads for the REST layer
├── mapper        Entity <-> DTO conversion (hand-written, no MapStruct)
├── service       Business logic (claim submission, status transitions, adjuster assignment, claim numbering)
├── api           REST controllers
├── scheduler     Background jobs (claim escalation sweep)
├── config        Spring configuration (async executor, Jackson, data seeding)
├── exception     Domain exceptions + a global @RestControllerAdvice
└── util          Small stateless helpers
```

Persistence is via Spring Data JPA / Hibernate against an in-memory H2
database (`ddl-auto: update`), which is representative of how the service
would run against PostgreSQL in a real deployment — no database-specific
SQL is used.

Notifications (claim submitted, status changed, escalation) are fired
asynchronously via a dedicated `ThreadPoolTaskExecutor` (`@Async`
methods on `NotificationService`) so that claim submission is not blocked
waiting on downstream messaging.

No Lombok, no MapStruct — all entities, DTOs, and mappers are written by
hand, matching this team's existing style guide.

## How to Build

Requires JDK 17+ and Maven 3.8+.

```bash
mvn clean package
```

This compiles the code, runs the unit test suite, and produces an
executable jar at `target/claimsflow-service-1.0.0.jar`.

## How to Run

```bash
mvn spring-boot:run
```

or

```bash
java -jar target/claimsflow-service-1.0.0.jar
```

The service starts on `http://localhost:8080` and seeds two demo
customers, two policies, and two adjusters on first startup (see
`DataSeedConfig`). An H2 console is available at
`http://localhost:8080/h2-console` (JDBC URL:
`jdbc:h2:mem:claimsflow`, user `sa`, empty password).

## API Documentation

| Method | Path                              | Description                              |
|--------|-----------------------------------|-------------------------------------------|
| POST   | `/api/claims`                     | Submit a new claim against a policy        |
| GET    | `/api/claims/{claimNumber}`       | Fetch a single claim by its reference number |
| GET    | `/api/claims?policyNumber=...`    | List claims for a policy                   |
| GET    | `/api/claims`                     | List all claims                            |
| PATCH  | `/api/claims/{claimNumber}/status`| Transition a claim's status                |
| GET    | `/api/policies/{policyNumber}`    | Fetch a policy                             |
| GET    | `/api/policies?customerId=...`    | List policies for a customer               |
| GET    | `/api/customers`                  | List customers                             |
| GET    | `/api/customers/{id}`             | Fetch a customer                           |

Example claim submission:

```bash
curl -X POST http://localhost:8080/api/claims \
  -H "Content-Type: application/json" \
  -d '{
        "policyNumber": "POL-AUTO-1001",
        "claimType": "ACCIDENT",
        "claimedAmount": 1500.00,
        "description": "Rear-end collision on I-95",
        "incidentDate": "2026-07-10"
      }'
```

Expected response (`201 Created`):

```json
{
  "id": 1,
  "claimNumber": "CLM-20260718-0001",
  "policyNumber": "POL-AUTO-1001",
  "claimType": "ACCIDENT",
  "status": "SUBMITTED",
  "claimedAmount": 1500.00,
  "description": "Rear-end collision on I-95",
  "incidentDate": "2026-07-10",
  "submittedAt": "2026-07-18T09:41:02.331",
  "assignedAdjusterName": "Carol Jennings"
}
```

## Expected Behaviour

- Every submitted claim receives a **unique** claim reference number of
  the form `CLM-YYYYMMDD-NNNN`, where `NNNN` is a zero-padded sequence
  that resets each calendar day.
- Claim status transitions are validated against the allowed state
  machine; invalid transitions return `409 Conflict`.
- Looking up a claim by its reference number (`GET
  /api/claims/{claimNumber}`) always returns exactly that claim.

## Known Production Issue (reported by Customer Support / QA)

> Customer Support has flagged several tickets where **two unrelated
> claims were issued the exact same claim reference number** (e.g., both
> assigned `CLM-20260715-0042`). This has caused the payments team to
> occasionally apply a payout to the wrong claim, and it's now blocking
> the auditors' reconciliation report for last quarter.
>
> QA has been unable to reproduce this by submitting claims one at a
> time in the staging environment. Support notes that the reports
> cluster around **lunchtime and end-of-day**, which is when call-center
> claim submission volume is highest.
>
> Separately, a handful of `GET /api/claims/{claimNumber}` requests from
> the case-management UI have started intermittently failing with a
> 500 error in production during the same time windows. The error does
> not show up in the staging environment.

You have been assigned this ticket. Investigate, reproduce, and fix the
root cause. See `hints.md` if you get stuck — it will not tell you where
the bug is or how to fix it, only how to investigate.
