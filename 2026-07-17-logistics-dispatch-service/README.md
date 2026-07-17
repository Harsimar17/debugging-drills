# Acme Logistics — Shipment Dispatch Service

## Business Requirements

Acme Logistics runs a warehouse network that receives orders from the e-commerce
front end and must hand each order off to a third-party carrier for physical
delivery. The **Dispatch Service** is the internal system responsible for:

1. Recording incoming customer **orders**.
2. Requesting a shipping rate quote from an available **carrier** for a given
   order/weight.
3. Creating a **shipment** record, assigning a tracking number, and notifying
   the chosen carrier that a pickup is required.
4. Automatically **retrying** shipments that failed to dispatch (e.g. due to a
   transient carrier outage), up to a configurable maximum number of attempts.
5. Receiving **webhook callbacks** from carriers when a shipment's delivery
   status changes (e.g. `DELIVERED`).

Client systems that call the dispatch API are expected to retry on timeout —
network hops between the storefront, the order gateway, and this service are
not always reliable, so callers pass an `Idempotency-Key` header with every
dispatch request. The service is required to guarantee that **retrying a
dispatch request with the same idempotency key never results in a second
shipment or a second carrier notification.**

## Architecture Overview

Single-module Spring Boot 3 / Java 17 application, layered by package:

```
com.acmelogistics.dispatch
├── api            REST controllers (Order, Shipment, Carrier webhook)
├── config         Spring configuration (async executor, data seeding)
├── common         Shared constants and reference-number generation
├── domain         JPA entities and enums
├── dto            Request/response payloads
├── mapper         MapStruct entity <-> DTO mappers
├── repository     Spring Data JPA repositories
├── scheduler      Scheduled retry job
├── service        Business logic
└── exception      Exception types + @RestControllerAdvice handler
```

**Storage:** H2, file-backed at `./data/dispatch-db` so state survives
restarts, in place of the PostgreSQL instance used in production.

**Key runtime components:**

- `ShipmentService.dispatchShipment(...)` — the main dispatch use case. Checks
  `IdempotencyService` before creating a new `Shipment`, requests a quote from
  `CarrierIntegrationService` (which simulates the latency of a real carrier
  rating API call), persists the shipment, and records `DispatchEvent` audit
  rows.
- `IdempotencyService` — in-memory tracker of idempotency keys that have
  already been processed, with a bounded retention window.
- `DispatchRetryScheduler` — a `@Scheduled` job (default every 10s) that
  re-attempts shipments left in `PENDING_RETRY`.
- `CarrierWebhookController` — receives carrier delivery-status callbacks.

## How to Build

Requires JDK 17+ and Maven 3.9+ (or use the included behavior of your IDE's
bundled Maven).

```bash
mvn clean verify
```

This compiles the project, runs annotation processing (Lombok + MapStruct),
and executes the test suite.

## How to Run

```bash
mvn spring-boot:run
```

or build a jar and run it directly:

```bash
mvn clean package
java -jar target/dispatch-service-1.0.0.jar
```

The service starts on `http://localhost:8080`. On first startup it seeds two
active carriers (`SWFT`, `MRDN`) and one inactive carrier (`CSTL`).

H2 console (for local inspection): `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:file:./data/dispatch-db`, user `sa`, empty password).

## API Documentation

### Create an order

```
POST /api/orders
Content-Type: application/json

{
  "customerName": "Jane Doe",
  "customerEmail": "jane@example.com",
  "deliveryAddress": "123 Main St, Springfield",
  "totalAmount": 149.99
}
```

`201 Created` with the created order, including its generated `id`.

### Dispatch a shipment for an order

```
POST /api/shipments
Content-Type: application/json
Idempotency-Key: <client-generated-uuid>

{
  "orderId": 1,
  "weightKg": 4.2,
  "preferredCarrierId": null
}
```

`201 Created` with the shipment (tracking number, assigned carrier, cost,
status). If the same `Idempotency-Key` is sent again, the service is expected
to return the **original** shipment rather than creating a new one.

### Get shipment status

```
GET /api/shipments/{id}
```

### Carrier delivery webhook

```
POST /api/webhooks/carrier/status
Content-Type: application/json

{
  "trackingNumber": "ACME-TRK-SWFT-482913",
  "carrierCode": "SWFT",
  "eventCode": "DELIVERED",
  "rawTimestamp": "2026-07-15T14:32:00Z"
}
```

## Expected Behaviour

- Every dispatch request with a **new** idempotency key produces exactly one
  new `Shipment` row and exactly one carrier notification event.
- Every dispatch request that **reuses** an idempotency key already seen by
  the service returns the shipment created by the original request — no new
  shipment, no duplicate carrier notification, no duplicate charge.
- Shipments that fail to dispatch move to `PENDING_RETRY` and are retried by
  the scheduled job until `DISPATCHED` or `dispatch.retry.max-attempts` is
  reached.

## Known Production Issue (reported by Carrier Billing, escalated by QA)

> "Under normal load everything reconciles fine. But during peak dispatch
> windows — when the storefront's retry logic re-sends the same dispatch
> request because an upstream hop timed out — we're seeing two different
> symptoms show up in support tickets: sometimes the retried request comes
> back with an error saying the shipment can't be found, even though the
> original request clearly succeeded; and sometimes carrier billing ends up
> with two invoiced shipments for what should have been a single request.
> Neither is reproducible by just clicking through the UI once — it only
> shows up when requests land close together. Needs investigation before the
> next peak sale event."

Your task: investigate, reproduce, identify the root cause, and fix it.
