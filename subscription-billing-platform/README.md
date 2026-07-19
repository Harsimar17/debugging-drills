# Subscription Billing Platform

A recurring-billing service for a SaaS product: customers subscribe to a plan,
get invoiced automatically on a schedule, and pay those invoices. Internal
teams (support, finance, ops) use the admin API to inspect and drive the
billing process.

This is a Spring Boot monolith backed by an embedded H2 database. It is
handed to you as a production codebase with a known outstanding issue —
see **Reported Production Issues** below. Your job is to reproduce it,
find the root cause, and fix it.

## Business Requirements

- A **Customer** can have one or more **Subscriptions**.
- A **Subscription** is tied to a **Plan** (price + billing cycle: monthly,
  quarterly, or annual) and has a `nextBillingDate`.
- Every day, the platform must find all `ACTIVE` subscriptions whose
  `nextBillingDate` has arrived and:
  1. Generate an **Invoice** for the amount due, covering the current billing
     period.
  2. Advance the subscription's `nextBillingDate` to the next cycle.
  3. Notify the customer (currently: log a "sending invoice" message; this
     stands in for a real email/notification integration).
- Each subscription must be billed **exactly once per billing period** —
  billing a customer twice for the same period, or not at all, is a
  correctness bug, not just a cosmetic issue.
- Support/ops staff can trigger the renewal process on demand via an admin
  endpoint (e.g. to process a customer's renewal immediately rather than
  waiting for the nightly job), in addition to the automatic nightly run.
- Customers can cancel a subscription; once cancelled it is no longer billed.
- Invoices can be marked paid by recording a payment against them.

## Architecture Overview

Single Maven module, layered by package:

```
com.acme.billing
├── domain        JPA entities (Customer, Plan, Subscription, Invoice, Payment) + enums
├── repository    Spring Data JPA repositories
├── dto           Request/response objects (plain classes, no Lombok/MapStruct)
├── mapper        Entity -> DTO mapping
├── service       Business logic
├── scheduler     @Scheduled cron job driving nightly renewals
├── api           REST controllers
├── exception     Custom exceptions + @RestControllerAdvice handler
├── util          Small stateless helpers (dates, money)
└── config        Scheduler and web configuration
```

**Stack:** Java 21, Spring Boot 3.2 (Web, Data JPA, Validation), H2 (in-memory),
Hibernate, JUnit 5 + Mockito. No Lombok, no MapStruct — all boilerplate is
hand-written.

**Renewal flow:** `SubscriptionRenewalScheduler` fires a cron job at 02:00
daily, which delegates to `BillingRenewalService.runRenewalBatch()`. The same
method can also be triggered synchronously via
`POST /api/admin/billing/renewals/run`. The batch processes subscriptions in
parallel (`parallelStream`) since invoicing a subscription involves a
(simulated) call to an external payment gateway, and a production-sized batch
can have many thousands of subscriptions due on the same day.

## How to Build

```bash
mvn clean package
```

This compiles the code and runs the test suite (`mvn test` to run tests only).

## How to Run

```bash
mvn spring-boot:run
# or
java -jar target/subscription-billing-platform-1.0.0.jar
```

The app starts on `http://localhost:8080`. On startup, `data.sql` seeds:

- 3 plans (`BASIC_MONTHLY`, `PRO_MONTHLY`, `ENTERPRISE_ANNUAL`)
- 40 customers, each with one `ACTIVE` subscription whose `nextBillingDate`
  is **today** — i.e. all 40 are due for renewal as soon as the app starts.

H2 console is available at `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:billingdb`, user `sa`, empty password).

## API Documentation

| Method | Path                                    | Description                                  |
|--------|------------------------------------------|-----------------------------------------------|
| POST   | `/api/customers`                        | Create a customer                             |
| GET    | `/api/customers/{id}`                   | Get a customer                                |
| GET    | `/api/customers`                        | List all customers                            |
| GET    | `/api/plans`                            | List active plans                             |
| POST   | `/api/subscriptions`                    | Create a subscription (`customerId`, `planCode`) |
| POST   | `/api/subscriptions/{id}/cancel`         | Cancel a subscription                         |
| GET    | `/api/subscriptions/{id}`               | Get a subscription                            |
| GET    | `/api/subscriptions?customerId={id}`    | List a customer's subscriptions               |
| GET    | `/api/invoices`                         | List all invoices                             |
| GET    | `/api/invoices/subscription/{id}`       | List invoices for a subscription              |
| GET    | `/api/invoices/{invoiceNumber}`         | Get an invoice by number                      |
| POST   | `/api/payments`                         | Record a payment (`invoiceId`, `paymentReference`) |
| POST   | `/api/admin/billing/renewals/run`       | Manually trigger the renewal batch now        |

All responses are wrapped as `{ "success": bool, "data": ..., "message": ... }`.

### Expected Behaviour

Calling `POST /api/admin/billing/renewals/run` should invoice every
subscription that is currently due, exactly once, and return a summary:

```json
{ "success": true, "data": { "due": 40, "renewed": 40, "skipped": 0, "failed": 0 } }
```

Calling it again immediately afterward (before the next billing period)
should renew nothing, since nothing is due yet:

```json
{ "success": true, "data": { "due": 0, "renewed": 0, "skipped": 0, "failed": 0 } }
```

## Reported Production Issues

The following tickets have come in from support, finance, and ops over the
last few weeks. They may or may not be related — that's for you to determine.

- **TICKET-101** (Critical, Support): "A customer emailed us screaming that
  they got charged twice for their Pro plan this month. We checked and there
  really are two invoices for the same billing period."

- **TICKET-104** (High, Finance): "Reconciliation between active
  subscriptions and invoices issued last night doesn't add up — we counted
  more invoices generated than there were subscriptions due for renewal."

- **TICKET-110** (Medium, Support Eng): "Admin dashboard renewal count
  looked higher than the number of paying customers on one of the days I
  checked. Might just be a reporting glitch, flagging it anyway."

- **TICKET-113** (Critical, Support): "Customer says they were billed twice
  within the same week for a monthly subscription that should only bill
  once a month."

- **TICKET-118** (High, Ops): "During a particularly busy end-of-month
  renewal window, we noticed the batch summary numbers looked inconsistent
  between two consecutive runs — some subscriptions seemed to get skipped,
  others processed more than once. Also saw a handful of error-level log
  lines during that window that we don't normally see."

We have not been able to reliably reproduce this in isolation from a single
request — it seems tied to load or timing somehow. Given the customer impact,
this is our top priority.

## New Feature Requirement

**REQ-201**: Add support for applying a one-time percentage-based discount
coupon when a subscription renews. The renewal API/batch should accept an
optional coupon percentage (e.g. 15 for 15% off) for a given renewal cycle,
apply it to the invoice amount, and the resulting `InvoiceDto`/API responses
should clearly reflect the discounted amount (as opposed to the plan's list
price). `MoneyUtils.applyPercentageDiscount` already exists as a building
block but is not wired into the renewal flow yet.

## Project Structure

```
subscription-billing-platform/
├── pom.xml
├── README.md
├── hints.md
└── src/
    ├── main/java/com/acme/billing/...
    ├── main/resources/{application.yml, logback-spring.xml, data.sql}
    └── test/java/com/acme/billing/...
```
