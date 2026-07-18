# Progressive Hints — MED-2291

Read these one at a time. Stop as soon as you have a lead worth chasing down
in the code, and go verify it yourself before reading further.

---

### Hint 1 — narrow the search space

This is not a database problem. There's no missing transaction, no wrong
isolation level, and the SQL is fine. Start by re-reading the whole booking
request path top to bottom, end to end, exactly as one request would execute
it: `AppointmentController` → `AppointmentBookingService.book(...)`. Ask
yourself: between the moment the code decides a slot is "still open" and the
moment it's saved as booked, what could another concurrent request possibly
observe or change?

<details><summary>Hint 2 — follow the data, not just the code</summary>

`book()` doesn't query the database directly for available slots — it goes
through `ProviderAvailabilityCacheService`. Spend time understanding exactly
what that class hands back, and to how many different callers. Is what it
returns really a fresh, private result for the caller who asked for it?

</details>

<details><summary>Hint 3 — Spring's cache abstraction is not a database</summary>

`@Cacheable` doesn't clone anything by default — whatever object your method
returns the first time is the *exact same object* every caller gets back for
every cache hit afterward, until the entry is evicted or expires. Two
different HTTP threads calling `getAvailableSlots(providerId, date)` around
the same time, for the same provider and day, can end up holding a reference
to the *same* `List` instance, at the *same* time. What does `ArrayList` (or
any plain `List`) guarantee you when two threads touch it concurrently?
Nothing.

</details>

<details><summary>Hint 4 — now go find where that list gets touched</summary>

Search `AppointmentBookingService` for anything that mutates the list
returned by the availability cache, rather than just reading from it. There's
exactly one such line, with a comment explaining why it seemed like a good
idea. Ask: what happens if two threads run the lines immediately before and
after that mutation at close to the same instant, on the same slot?

</details>

<details><summary>Hint 5 — the actual race, spelled out</summary>

Thread A and Thread B both call `getAvailableSlots(...)` for the same
provider/date while the cache entry is still warm. Both get the same `List`
object containing the same `TimeSlot` object for the slot in question. Both
run `.stream().filter(...).findFirst()` and both find it — because as far as
either thread can tell at that instant, nobody has removed it yet. Only after
that do they get around to mutating the slot and saving it. There is nothing
in this path — no lock, no atomic check-and-reserve, no unique constraint at
the database level backing up the in-memory check — that prevents both
threads from getting past the "is it still available?" check before either
one has recorded that it's taken it.

</details>

<details><summary>Hint 6 — where NOT to spend more time</summary>

`TimeSlot` has no `@Version` field and there's no unique index on
`appointments.slot_id` — that's a real gap, but adding either of those only
turns the *symptom* into a 500 for the loser instead of a silent double
booking. It doesn't explain why the check ever let two requests through in
the first place. The actual defect is earlier in the flow, in how "is this
slot available" gets answered for concurrent callers. Fix that, and decide
for yourself whether a DB-level safety net is *also* worth adding as a second
line of defense.

</details>
