# MedLink — Clinic Scheduling Platform

## Business Requirements

MedLink runs the booking backend for a network of outpatient clinics. Patients
(via a web/mobile front end that calls this service) need to:

1. Browse a **provider's** open appointment slots for a given day.
2. **Book** an appointment against one of those open slots, receiving a
   confirmation code.
3. **Cancel** an appointment, which frees the slot back up for other patients.
4. Automatically receive a **reminder** notification the evening before their
   appointment.

Clinic operations also require:

- Any slot that passes its date without ever being booked is automatically
  cancelled by a nightly housekeeping job (so calendars don't accumulate
  stale entries).
- The **one hard rule** the business has been explicit about since the
  project kicked off: *a single appointment slot can only ever be held by one
  confirmed appointment.* Double-booking a provider is a patient-safety and
  trust issue, not just a cosmetic bug.

## Architecture Overview

Multi-module Gradle build, Java 21, Spring Boot 3.3. Layered by module rather
than by package, so each concern has a clear dependency direction:

```
clinic-common      Shared exceptions + utilities (no framework dependencies)
clinic-domain       JPA entities + enums
clinic-repository   Spring Data JPA repositories
clinic-service      DTOs, MapStruct mappers, business services, availability cache
clinic-scheduler    @Scheduled jobs (reminders, stale-slot cleanup)
clinic-api          REST controllers + global exception handling
clinic-app          Spring Boot entry point, cache/config wiring, data seeder
```

Dependency direction: `clinic-app` → (`clinic-api`, `clinic-scheduler`) →
`clinic-service` → `clinic-repository` → `clinic-domain` → `clinic-common`.

Stack:

- **Java 21 / Spring Boot 3.3** (Spring MVC, Spring Data JPA)
- **H2** in-memory database (seeded on startup — no external DB needed)
- **Caffeine** as the Spring Cache provider for the provider-availability
  read path
- **Lombok** for boilerplate, **MapStruct** for entity↔DTO mapping
- **JUnit 5 / Mockito / AssertJ** for tests

## Build & Run

Requires JDK 21 and Gradle 8.x on your `PATH` (no wrapper is checked in).

```bash
cd 2026-07-18-medlink-clinic-scheduling
gradle build
gradle :clinic-app:bootRun
```

The app starts on `http://localhost:8080` and seeds 3 providers, 3 patients,
and a week of half-hour weekday slots (9am–12pm) per provider on first boot.

H2 console (optional, for poking at the seeded data): `http://localhost:8080/h2-console`
— JDBC URL `jdbc:h2:mem:clinicdb`, user `sa`, empty password.

## API Documentation

| Method | Path                                          | Description                              |
|--------|-----------------------------------------------|-------------------------------------------|
| GET    | `/api/providers?specialty={specialty}`        | List providers, optionally by specialty   |
| GET    | `/api/providers/{id}`                         | Get a single provider                     |
| GET    | `/api/providers/{id}/availability?date=YYYY-MM-DD` | Open slots for a provider on a day  |
| POST   | `/api/patients`                               | Register a patient                        |
| GET    | `/api/patients/{id}`                          | Get a single patient                      |
| GET    | `/api/patients`                               | List all patients                         |
| POST   | `/api/appointments`                           | Book an appointment                       |
| DELETE | `/api/appointments/{id}`                      | Cancel an appointment                     |

`POST /api/appointments` body:

```json
{
  "patientId": 1,
  "providerId": 1,
  "slotId": 3,
  "date": "2026-07-20"
}
```

Success → `201 Created` with the confirmation code. If the slot isn't open
anymore → `409 Conflict`.

## Expected Behaviour

- Booking an open slot always succeeds exactly once per slot; every other
  request for that same slot (before or after) gets a `409` with
  `"Slot ... is no longer available"`.
- Cancelling an appointment immediately makes its slot bookable again.
- The evening reminder job (`AppointmentReminderJob`, cron `0 0 18 * * *`)
  sends a reminder for every confirmed appointment happening tomorrow.
- The nightly cleanup job (`StaleSlotReleaseJob`, cron `0 30 0 * * *`)
  cancels any slot whose date has passed while it was still marked
  `AVAILABLE`.

## Known Production Issue (reported by Clinic Ops / QA)

> **Ticket:** MED-2291 — Severity: **Critical**
> **Reported by:** Clinic front-desk staff, escalated by QA
>
> "We've had three clinics report the same thing this week: two patients show
> up for the exact same appointment slot with the same provider, and both of
> them have a valid confirmation email from us. It seems to happen on busy
> mornings right when a popular slot opens up — we assume it's when a lot of
> people are trying to book around the same time. Load testing in staging with
> a single request at a time never reproduces it. It only seems to happen
> under real concurrent traffic."

Only the symptom is described above — QA has not identified a root cause.

## Reproducing It

```bash
cd 2026-07-18-medlink-clinic-scheduling
gradle :clinic-service:test --tests "com.medlink.clinic.service.ConcurrentBookingSimulationTest" --rerun
```

This test fires a burst of concurrent booking requests at the same open slot
and asserts exactly one should win. It does **not** fail every run — rerun it
a handful of times if it passes once. You can also drive it end-to-end
against the running app: start `clinic-app`, note an open slot id from
`GET /api/providers/1/availability?date=...`, then fire ~20 concurrent
`POST /api/appointments` requests for that same slot/provider/date and check
how many come back `201 Created`.
