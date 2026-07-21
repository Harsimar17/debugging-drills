# Hints

These are investigative nudges, not answers. They point you at the *kind* of
thing to look for. No file names, no line numbers, no solutions. Work the
tickets like production incidents: reproduce first, then read the code path the
request actually travels.

## General approach

- Turn the symptoms into a failing test. If you can't reproduce it on demand,
  you don't understand it yet.
- For each ticket, trace the request end to end: controller → service →
  repository → entity. The bug is somewhere on that path.
- Don't trust method and query names to be doing what they say. Read what they
  actually compute.
- Add a couple of assertions around the numbers you *expect* vs. what you *get*.
  The gap usually tells you exactly which step is wrong.

## LOYAL-611 (tier bonus sometimes missing)

- Reproduce with a non-Standard member and watch how many ledger rows are
  actually persisted versus how many the code intended to persist.
- When two freshly-created objects are put into the same collection before they
  hit the database, ask what makes the collection consider them "the same".
- What is true about a JPA entity's identifier *before* it is saved?

## LOYAL-627 (balance doesn't drop after redemption)

- Compare the *intent* described in the balance calculation's documentation with
  the set of rows it actually sums.
- Redemptions and expiries are stored as their own ledger movements. Are they
  being counted?
- Seed member 1 already has an earn, an earn, and a redemption. What *should*
  their available balance be, and what does the summary endpoint report?

## LOYAL-644 (summary 500s for some members, not others)

- Which members trigger it? Look for a numeric relationship between the failing
  members' balances and a configured constant.
- The stack trace in the logs names the exact operation. Reproduce with a
  balance that has that property, and with one that doesn't.
- A trailing, reassuring-looking call doesn't help if an earlier step already
  threw.

## LOYAL-659 (points expire a day early near month boundaries)

- Two different parts of the system decide "what time is it now". Are they using
  the same clock/zone?
- Look at how an entry's expiry instant is written versus how the sweep decides
  the entry has elapsed.
- Try running the service (or the relevant unit under test) with a non-UTC
  default time zone and an entry expiring close to midnight.

## LOYAL-702 (earning crashes for Platinum only)

- The crash is tier-specific. Find every place tier drives a lookup or branch,
  and check that *every* tier value is actually handled.
- Standard, Silver and Gold work. What's different about the fourth one?

## VANTAGE-REQ-118 (points transfer)

- The service skeleton and endpoint exist and return `501`. Re-read the
  acceptance criteria carefully — points 2 and 4 are where naive implementations
  lose marks.
- Reuse the existing ledger model; don't invent a parallel balance store.

Good luck. Tell me **"I have fixed it."** when you're ready for review.
