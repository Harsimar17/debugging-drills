# SOLUTION — spoilers below

Stop here unless you've tried. Seriously.

---

## Root cause

`AccountRepository.getOrCreate` is a **check-then-act race** (a lost-update /
"time-of-check to time-of-use" bug):

```java
Account existing = accounts.get(id);   // (1) check
if (existing != null) return existing;
Account created = new Account(id, currency);
accounts.put(id, created);             // (2) act
return created;
```

Using a `ConcurrentHashMap` makes each *individual* call (`get`, `put`) atomic, but
it does **not** make the *sequence* atomic. When two settlement threads reference
the same account id for the first time simultaneously:

1. Thread A: `get` → `null`
2. Thread B: `get` → `null`  (A hasn't `put` yet)
3. Thread A: `put(id, accountA)`, returns `accountA`
4. Thread B: `put(id, accountB)`, returns `accountB`  ← overwrites A's entry

Now there are **two distinct `Account` objects** for the same id. Thread A does its
transfer against `accountA`; Thread B does its transfer against `accountB`. Each is
internally correct (balances are updated under `synchronized`). But the map only
holds `accountB`. Any debit/credit A applied to `accountA` is **orphaned** — it
never appears in `repo.all()`.

Because a posting is a *pair* (debit here, credit there), losing one leg of some
postings while keeping the other unbalances the books. Whether the net comes out
positive or negative depends on whether the orphaned legs were mostly debits or
credits — which is why the imbalance is a random ±few minor units and changes every
run. The race window is tiny and only exists on an account's *first* touch, so it
never reproduces in a debugger and never single-threaded.

## Why the single-threaded tests passed

With one thread there is no interleaving: step (1) and (2) always complete before
anything else touches the map, so `getOrCreate` never produces a duplicate. The
logic is 100% correct sequentially — the bug is purely in the *concurrent*
interleaving, which those tests never exercise.

## The fix

Make get-or-create atomic. `ConcurrentHashMap.computeIfAbsent` does exactly this —
the mapping function runs at most once per absent key, and every caller gets the
**same** instance:

```java
public Account getOrCreate(AccountId id, Currency currency) {
    return accounts.computeIfAbsent(id, k -> new Account(k, currency));
}
```

That's the whole fix. One line.

### Equivalent alternatives
- `putIfAbsent` + use the returned value:
  ```java
  Account created = new Account(id, currency);
  Account prev = accounts.putIfAbsent(id, created);
  return prev != null ? prev : created;
  ```
- Pre-create all accounts at startup so `getOrCreate` never races (works, but
  brittle and doesn't fix the actual defect).

### Why the locking in `LedgerService` was a red herring
The `synchronized (first) { synchronized (second) { ... } }` block with ordered
lock acquisition is genuinely correct and even avoids deadlock. It lulls you into
thinking concurrency was handled carefully. But it locks the `Account` *objects* —
and the bug is that two threads are holding **different objects for the same
account**, so they lock different monitors and never actually exclude each other.
The defect is upstream, in how accounts are obtained, not in how they're locked.

## Verify

```bash
cd settlement-engine
for i in $(seq 1 20); do mvn -q -pl settlement-ledger test -Dtest=LedgerConcurrencyTest \
  || echo "FAILED on iteration $i"; done
mvn -pl settlement-app exec:java   # NET IMBALANCE: 0 every time
```

## The lesson

> A thread-safe *collection* does not give you a thread-safe *sequence of
> operations* on it. Check-then-act (`get` then `put`, `containsKey` then `add`,
> read-modify-write) is the single most common concurrency bug in Java services.
> Reach for the atomic compound operations (`computeIfAbsent`, `merge`,
> `putIfAbsent`, `compute`) instead of rolling your own with separate calls.
