# Debugging Hints

Use these one at a time — try to make progress before reading the next one.

1. A single, isolated call to the renewal endpoint is unlikely to show
   anything wrong. Think about *how* the tickets describe the problem
   showing up: load, timing, busy periods. Try to reproduce it under those
   conditions rather than with one request.

2. Read `application.yml` and anything related to scheduling. Is there
   anything configured there that changes the usual assumption that
   `@Scheduled` jobs run one at a time?

3. Look for every place the renewal batch can be started — not just the
   cron job. Is there more than one way to kick it off? What happens if two
   of those happen close together?

4. The unit tests for the renewal service all pass. That's not the same as
   the renewal service being safe. What conditions do those tests *not*
   exercise?

5. When something is processed by more than one thread at once, "check if
   it's already been done, then do it" is a classic trap. Ask whether the
   check and the action are actually atomic together, and what data
   structure is backing that check.

6. Not every class in `java.util` is safe to share across threads. If
   something in the billing path is being read and written from multiple
   threads at once, check whether it was built for that.

7. Turn on DEBUG logging (already enabled for `com.acme.billing`) and fire
   two renewal requests back-to-back rather than one at a time. Compare the
   `due` / `renewed` / `skipped` / `failed` counts across the two responses,
   and check `/api/invoices` afterward for a subscription that got billed
   more than once.

8. Once you can reproduce it reliably, think about *why* the safeguard that
   was clearly intended to prevent this doesn't actually work under
   concurrent access — and what a fix would need to guarantee to be safe
   the next time two triggers overlap.
