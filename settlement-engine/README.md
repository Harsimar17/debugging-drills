# Settlement Engine — Debugging Challenge

A multi-module Java 17 / Maven project modelling the **post-trade settlement ledger**
that sits behind an exchange. As trades clear, money moves between counterparty
accounts. The single sacred invariant of any such system:

> **Money is never created or destroyed.** Every posting debits one account and
> credits another by the same amount, so the sum of all account balances must be
> exactly **zero**, always.

## Modules

| Module               | Responsibility                                              |
|----------------------|-------------------------------------------------------------|
| `settlement-domain`  | Value types: `Money`, `Currency`, `Account`, `AccountId`, `SettlementInstruction` |
| `settlement-fx`      | `FxConverter` — cross-currency conversion on a rate table   |
| `settlement-ledger`  | `AccountRepository` + `LedgerService` — the double-entry posting engine |
| `settlement-app`     | `SettlementSimulator` — runs a concurrent settlement burst  |

## The ticket

```
JIRA  BUG-5127   Severity: P1   Component: settlement-ledger
Title: End-of-day reconciliation shows a few cents of imbalance under peak load

Reconciliation occasionally reports the clearing ledger a few minor units out of
balance. It only happens on high-volume days. It never reproduces in a debugger,
never on a single-threaded rerun, and the amount is different every time
(sometimes positive, sometimes negative). Unit tests are green.

Finance is not amused. Find it.
```

## Reproduce it

```bash
cd settlement-engine
mvn clean test           # LedgerConcurrencyTest fails; the two single-threaded tests pass
```

Or watch it leak live:

```bash
mvn -DskipTests install
mvn -pl settlement-app exec:java     # run it a few times; NET IMBALANCE drifts off zero
```

## Your mission

1. Reproduce the imbalance.
2. Explain the root cause — *why* money appears to leak, precisely.
3. Fix it so `LedgerConcurrencyTest` passes reliably (run it many times) **without**
   weakening the concurrency (don't just wrap everything in one global lock and
   call it a day — or if you do, understand what you gave up).
4. Bonus: explain why the two single-threaded tests never caught it.

## Rules of engagement (to keep it a real debugging exercise)

- Don't open `SOLUTION.md` until you've genuinely tried. It's the answer key.
- `HINTS.md` has progressive hints — read them one at a time, not all at once.
- The bug is a single root cause in the production code. The tests, the domain
  types, and the FX module are all correct — don't waste time "fixing" them.

Good hunting.
