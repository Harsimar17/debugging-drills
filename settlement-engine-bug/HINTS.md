# Progressive Hints

Read these ONE AT A TIME. Stop as soon as you have a lead and go back to the code.

---

### Hint 1 — where to look
The `Money`, `Account`, and FX math are all exact (integer minor units). The
imbalance is not a rounding error. It's a **concurrency** defect, and it lives in
`settlement-ledger`. Two files. That's your whole search space.

<details><summary>Hint 2 — reframe the symptom</summary>

The debit and the credit inside `LedgerService.post` are always equal and always
run together under locks. So the arithmetic is fine. If money still goes missing,
ask a different question: **is every debit and credit actually landing on the
account the repository will later report on?**
</details>

<details><summary>Hint 3 — trust nothing you "know" is thread-safe</summary>

`AccountRepository` uses a `ConcurrentHashMap`, so it *looks* thread-safe. But a
concurrent map only makes each individual operation atomic. Look very carefully at
`getOrCreate`. How many separate operations on the map does it perform, and what
can another thread do *in between* them?
</details>

<details><summary>Hint 4 — the exact race</summary>

`getOrCreate` does: `get` → (null?) → `new Account(...)` → `put`. That's
check-then-act. Two threads can both see `null` for the same brand-new account id,
each construct a **different** `Account` object, and each `put` it — the second
`put` overwrites the first in the map. Now trace what happens to the transfer being
done on the *losing* `Account` instance...
</details>

<details><summary>Hint 5 — why money appears to leak</summary>

Both threads hold real references to their own `Account` instance and correctly
debit/credit it under `synchronized`. But only ONE of those instances survives in
the map. The debit or credit applied to the **orphaned** instance is invisible to
`repo.all()` at the end — so its half of a double-entry posting vanishes from the
totals, and the ledger no longer sums to zero. The sign and size depend on which
orphaned entries were debits vs credits — hence the random ±few units.
</details>

---

When you think you have the fix, verify by running the concurrency test in a loop:

```bash
cd settlement-engine
for i in $(seq 1 20); do mvn -q -pl settlement-ledger test -Dtest=LedgerConcurrencyTest || echo "FAILED on iteration $i"; done
```

A correct fix passes all 20 (and the simulator prints `NET IMBALANCE: 0` every run).
