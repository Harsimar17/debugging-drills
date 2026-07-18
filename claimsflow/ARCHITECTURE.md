# Architecture Overview

## Runtime shape

ClaimsFlow runs as a single Spring Boot process behind an embedded Tomcat
servlet container (default thread pool: 200 threads). There is no
external cache layer, message broker, or second service — this is
intentionally a monolith for this line of business.

```
HTTP client
   │
   ▼
ClaimController / PolicyController / CustomerController   (api)
   │
   ▼
ClaimServiceImpl / PolicyServiceImpl                       (service)
   │
   ├──> ClaimNumberGeneratorService   (claim reference numbering)
   ├──> AdjusterAssignmentService     (least-loaded adjuster lookup)
   ├──> ClaimStatusService            (status state machine + audit trail)
   └──> NotificationService           (@Async notifications)
   │
   ▼
ClaimRepository / PolicyRepository / ...                   (repository, Spring Data JPA)
   │
   ▼
H2 (in-memory; PostgreSQL-compatible SQL only)
```

## Request handling model

Every inbound HTTP request is handled on its own Tomcat worker thread.
All `@Service` and `@Component` beans are **singletons** by default
(standard Spring bean scope) — the same `ClaimServiceImpl`,
`ClaimNumberGeneratorService`, etc. instances are shared and invoked
concurrently by every request thread. This is standard Spring MVC
behaviour and is true of every service in this codebase, not just claim
submission.

`ClaimServiceImpl.submitClaim()` runs inside a single `@Transactional`
boundary: it looks up the policy, generates a claim number, optionally
assigns an adjuster, and persists the claim, all in one transaction.
Notification dispatch happens after the transaction via
`NotificationService`, which delegates to the async executor configured
in `AsyncConfig`.

## Data model

- `Customer` 1—* `Policy` 1—* `Claim`
- `Claim` 1—* `ClaimDocument`
- `Claim` 1—* `ClaimStatusHistory` (append-only audit trail)
- `Claim` *—1 `Adjuster` (nullable — a claim may be unassigned if no
  adjuster in the region has capacity)

`Claim.claimNumber` is the externally visible identifier. It is produced
once, at submission time, by `ClaimNumberGeneratorService` and stored
alongside the surrogate `id` primary key.

## Claim status state machine

Enforced centrally in `ClaimStatusService`:

```
SUBMITTED ──> UNDER_REVIEW ──> APPROVED ──> PAID ──> CLOSED
    │              │  │
    │              │  └──> REJECTED ──> CLOSED
    │              └──> PENDING_DOCUMENTS ──> UNDER_REVIEW
    └──────────────────> REJECTED
```

Any transition not in this table throws `InvalidClaimStateException`
(mapped to `409 Conflict`).

## Scheduled work

`ClaimEscalationScheduler` runs every 30 minutes and escalates any claim
that has been in `UNDER_REVIEW` for more than 72 hours. It queries via
`ClaimRepository.findStaleClaims(...)` and fires an async notification
per stale claim.

## Deployment notes

The `application.yml` ships with H2 for local development and the demo
environment described in this repository. In the real production
deployment this profile is swapped for a PostgreSQL datasource via an
externalized `application-prod.yml` (not included here) — no
H2-specific SQL or dialect features are used anywhere in the codebase,
so the swap is transparent at the JPA layer.
