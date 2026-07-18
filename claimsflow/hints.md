# Hints

Use these progressively — try to make real progress between each one
before reading the next. None of these tell you which file or line the
bug is in.

### Hint 1 — Reframe the symptom

Support's report says duplicates "don't reproduce" when claims are
submitted one at a time in QA, but do show up under real call-center
volume. That's a strong signal about the *conditions* under which the
bug appears, not just the bug itself. What's different about
"one request at a time" versus "many requests at once" from the
application's point of view?

### Hint 2 — Follow one request all the way through

Pick the endpoint customers hit to submit a claim and trace, line by
line, everything that happens between the HTTP request coming in and
the claim reference number being decided. Don't assume any single step
is safe just because it looks simple — write down, for each step,
whether it reads or writes any state that isn't local to that one
request.

### Hint 3 — Remember how Spring wires things up

Unless a bean is explicitly given a different scope, Spring creates
exactly one instance of it for the whole application and hands that
same instance to every request thread. Any field on such a bean that
gets mutated while handling a request is, by definition, shared across
every concurrent request. Go looking for beans that hold mutable state
in an instance field.

### Hint 4 — Look at the data structure, not just the logic

If you find a spot that reads a value, computes something from it, and
writes it back — ask whether that "read, compute, write" sequence is
guaranteed to happen as one atomic step, or whether another thread
could interleave in the middle of it. Then ask the same question about
the container holding the value: is it a data structure documented as
safe for concurrent access, or an ordinary one that happens to work
fine in single-threaded tests?

### Hint 5 — Reproduce it deliberately

Write a small test (or a quick `curl`-in-a-loop / `ExecutorService`
harness) that fires many concurrent `POST /api/claims` requests at a
running instance of the service and then checks the returned claim
numbers for duplicates. If you can make the bug happen on demand in
under a minute, you're very close to knowing exactly where it lives.

### Hint 6 — Check what the database would have told you

Once you've reproduced it, look at how `claimNumber` is declared in the
JPA entity and how the table is created. Would a uniqueness constraint
at the database level have caught this earlier — in QA, or even in
production the very first time it happened? Why doesn't one exist here?

### Hint 7 — The second symptom is a clue, not a separate bug

QA also mentioned intermittent `500` errors on `GET
/api/claims/{claimNumber}` during the same busy windows. Think about
what `ClaimRepository.findByClaimNumber` does when its query criteria
matches more than one row, and what that implies about the state of
the data at that point.
