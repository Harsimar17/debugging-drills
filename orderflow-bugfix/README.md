# OrderFlow-bugfix

A small multi-module order-pricing engine.

```
orderflow-domain    Order, CustomerTier, timestamp parsing
orderflow-pricing   PricingEngine + pricing rules (loyalty discount, weekend surcharge)
orderflow-service   OrderProcessor (parallel batch pricing), test-data factory
orderflow-app       Main — the BUG-4471 reproduction harness
```

## Build & run

```bash
cd orderflow-bugfix
mvn -q clean install
mvn -q -pl orderflow-app exec:java
```

You can vary the load (great for narrowing this bug down):

```bash
# exec.args = "<orderCount> <workerThreads> <rounds>"
mvn -q -pl orderflow-app exec:java -Dexec.args="5000 8 10"   # default: heavy concurrency
mvn -q -pl orderflow-app exec:java -Dexec.args="5000 1 10"   # same work, single worker
```

---

## 🐞 BUG-4471 — Nightly revenue report crashes under load

**Reporter:** QA (Priya)  **Severity:** High  **Status:** Open
**Component:** orderflow-service / pricing

**Summary**
The nightly revenue reconciliation job dies under production load. The stack
trace makes no sense: a `NumberFormatException` for an *empty* or garbage string,
thrown from deep inside `java.text` — even though **every timestamp in the batch
is the same clean, valid string** (`2026-07-15 10:00:00`). There is no bad data.

On the rare nights it doesn't crash, Finance has separately reported the total
coming out **slightly too high** — never too low.

```
Round  3: THREW java.lang.RuntimeException: java.lang.NumberFormatException: For input string: ""
Round  4: THREW java.lang.RuntimeException: java.lang.NumberFormatException: For input string: "15E"
```

**Steps to reproduce**
1. Run the harness with the default heavy-concurrency config. Every order is
   STANDARD tier, flat 100.00, placed on a **Wednesday** — so the total *must*
   be `orderCount * 100.00` with no discounts and no weekend surcharge.
2. Watch it throw `NumberFormatException` from a parser that was handed a
   perfectly valid, hard-coded date string.

**Expected:** every round reconciles to exactly `orderCount * 100.00`.
**Actual:** under concurrency it throws (occasionally instead reports a total
that's too high). Every timestamp fed in is identical and valid.

**Notes from the dev who looked first**
- "Can't reproduce when I step through it in the debugger — passes every time."
- "Run it single-threaded (`exec.args=\"5000 1 10\"`) and it's rock solid.
   Crank the thread count and it falls over. So it's a threading thing... but
   the pricing fan-out in `OrderProcessor` looks correct to me?"
- "The bad total, when it happens, is always *high*, never *low*. Weird."
- "Rolling back the PERF-1183 change makes it stop — but that was 'just a
   micro-optimization to timestamp parsing' and it passed review. Nobody can see
   how it could crash the report or move a total."

---

## Your job

1. Reproduce it (default config crashes reliably; single worker never does).
2. Find the **one** root cause.
3. Explain the paradox: how can a hard-coded, valid date string produce a
   `NumberFormatException: ""`? And why does the same root cause also make the
   total come out *high*?
4. Fix it so the harness passes every round, every time, at any thread count —
   **without** throwing away the PERF-1183 performance win.

The concurrency in `OrderProcessor` is a deliberate red herring: each task is
independent and the partial sums are added on one thread. Convince yourself it's
correct before you blame it — the real culprit is somewhere that code *reads
from*, not the loop itself.

Stuck? See [HINTS.md](HINTS.md) — but try to crack it cold first. That's the point.

---

## 🐞 BUG-4472 — Nightly revenue report reconciles ~15% too high

**Reporter:** Finance (via QA)  **Severity:** High  **Status:** Open
**Component:** orderflow-service / pricing

> A *different* ticket from BUG-4471 above. BUG-4471 (the shared-formatter
> concurrency crash) has already been fixed — each pricing task now gets its own
> `SimpleDateFormat`. BUG-4472 is a new, **deterministic** defect: no crash, no
> flakiness, just a wrong total.

**Summary**
The nightly reconciliation job runs to completion without errors, but the total
is wrong. The batch is the usual deterministic one — every order is **STANDARD**
tier, flat **100.00**, placed on a **Wednesday** (`2026-07-15 10:00:00`) — so the
total *must* be `orderCount * 100.00 = 500000.00`.

Instead every round reports **575000.00** — exactly **15% high** — and it's
rock-solid reproducible: the same number every round, whether you run 8 workers
or 1.

```
Round  1: actual=575000.00  ->  FAIL  <-- revenue mismatch
...
10 of 10 rounds failed to reconcile.
```

**Steps to reproduce**

```bash
cd orderflow-bugfix
mvn -q clean install -DskipTests
mvn -q -pl orderflow-app exec:java                          # 8 workers → 575000.00
mvn -q -pl orderflow-app exec:java -Dexec.args="5000 1 3"   # 1 worker  → still 575000.00
```

**Expected:** every round reconciles to exactly `500000.00`.
**Actual:** every round is `575000.00`.

---

### Your job (BUG-4472)

1. Reproduce it (above — it fails every time, so this one's easy to pin down).
2. Find the **one** root cause. Why is a batch of plain weekday orders getting a
   **15%** bump? Where does 15% come from in this codebase, and what makes it fire?
3. Explain the paradox: the timestamp fed in is a hard-coded, valid Wednesday
   (`2026-07-15 10:00:00`). So why does the pricing layer think it's a weekend?
4. Fix it so the harness passes every round at any thread count.

This one is **not** concurrency — single-worker fails identically. Ignore the
fan-out loop in `OrderProcessor`; look at what each task is *handed*. Follow the
15%: grep for it, find the rule that applies it, then work backwards to *why*
that rule triggers — and when you reach the parsing layer, compare the format
string actually in use against the timestamp contract documented in `Order.java`.
