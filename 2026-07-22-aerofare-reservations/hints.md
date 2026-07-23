# Hints

Investigative nudges, not answers — no file names, no line numbers, no fixes.
Work each ticket like a production incident: reproduce first, then trace the
request through the modules (`api → service → repository → domain`) and read what
the code *actually* computes versus what its name promises.

## General approach

- Turn each symptom into a failing check first. `reproduce_issues.py` gives you a
  starting point; extend it if you need to.
- This is a multi-module build. The bug for a given ticket may live in a
  different module from where the symptom surfaces (an API response driven by a
  repository query, a service value derived from a domain method).
- Distrust helpers whose names sound right. "duration", "total balance",
  "available", "unique" — check that each does what it claims.
- When numbers are off, print expected vs actual at each hop; the hop where they
  diverge is your bug.

## AERO-4102 (durations wrong / negative)

- What information does a correct elapsed-time calculation need that a plain
  subtraction of two timestamps throws away?
- Departure and arrival are stored in *different* places' local time. Look at
  what the airport records alongside its code.
- Find a westbound trans-oceanic flight and compute its duration by hand.

## AERO-4118 (flights missing from search)

- Compare a flight that shows up with one that doesn't. What's numerically
  different about the one that vanishes?
- Read the search query's seat-availability condition very literally. What
  happens when availability is *exactly* the number of seats requested?
- Boundary conditions: `>` vs `>=`.

## AERO-4135 (500 for a particular party size)

- Which party sizes fail, which succeed? Factor the numbers.
- The stack trace names the operation. It happens while turning one amount into a
  per-person share.
- What does `BigDecimal` do when a quotient can't be represented exactly and you
  didn't say how to round?

## AERO-4150 (a passenger goes missing)

- Reproduce by booking two travellers who share something. What do they share
  that makes the system treat them as the same person?
- When distinct objects are gathered into a collection that removes "duplicates",
  what decides that two of them are duplicates?
- Look at how a passenger decides whether it equals another passenger.

## AERO-4177 (premium cabin priced as economy)

- The effect depends on what was priced just before — that's a strong hint about
  *state being remembered* between calls.
- If a computed price is stored for reuse, what is it stored *under*? Is that key
  enough to distinguish two genuinely different prices for the same flight?
- Price the same flight twice in different cabins and watch what comes back the
  second time.

## AERO-REQ-210 (refund)

- The skeleton and endpoint exist and return `501`. Re-read the acceptance
  criteria — points 2 (non-refundable), 4 (release seats + statuses) and 5
  (atomicity) are where naive attempts lose marks.
- Reuse the existing seat-inventory and payment mechanisms; don't invent parallel
  ones.

Tell me **"I have fixed it."** when you're ready for review.
