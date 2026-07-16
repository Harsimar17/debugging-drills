package com.acme.settlement.ledger;

import com.acme.settlement.domain.Account;
import com.acme.settlement.domain.Money;
import com.acme.settlement.domain.SettlementInstruction;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Posts settlement instructions to the ledger as double-entry transfers:
 * every posting debits exactly one account and credits exactly one other
 * account by the same amount, so the total money in the system is invariant.
 *
 * Postings for the same pair of accounts are serialized by locking both
 * accounts. Locks are always acquired in a consistent order (by account id) to
 * avoid deadlock between two transfers touching the same accounts in opposite
 * directions.
 */
public final class LedgerService {

    private final AccountRepository repository;
    private final AtomicLong postedCount = new AtomicLong();

    public LedgerService(AccountRepository repository) {
        this.repository = repository;
    }

    public void post(SettlementInstruction instruction) {
        Money amount = instruction.amount();

        Account from = repository.getOrCreate(instruction.from(), amount.currency());
        Account to = repository.getOrCreate(instruction.to(), amount.currency());

        // Deterministic lock ordering to prevent deadlock.
        Account first = orderFirst(from, to);
        Account second = (first == from) ? to : from;

        synchronized (first) {
            synchronized (second) {
                from.debit(amount);
                to.credit(amount);
            }
        }
        postedCount.incrementAndGet();
    }

    private static Account orderFirst(Account a, Account b) {
        int cmp = a.id().value().compareTo(b.id().value());
        return cmp <= 0 ? a : b;
    }

    public long postedCount() {
        return postedCount.get();
    }
}
