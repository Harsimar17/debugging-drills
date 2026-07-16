package com.acme.settlement.ledger;

import com.acme.settlement.domain.Account;
import com.acme.settlement.domain.AccountId;
import com.acme.settlement.domain.Currency;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store of settlement accounts, keyed by account id.
 *
 * Accounts are created lazily the first time they are referenced by a
 * settlement instruction. Backed by a {@link ConcurrentHashMap} so it can be
 * read and written from all of the settlement worker threads at once.
 */
public final class AccountRepository {

    private final Map<AccountId, Account> accounts = new ConcurrentHashMap<>();

    /**
     * Returns the account for {@code id}, creating it (with a zero balance) on
     * first reference.
     */
    public Account getOrCreate(AccountId id, Currency currency) {
        Account existing = accounts.get(id);
        if (existing != null) {
            return existing;
        }
        Account created = new Account(id, currency);
        accounts.put(id, created);
        return created;
    }

    public Account get(AccountId id) {
        return accounts.get(id);
    }

    public Collection<Account> all() {
        return accounts.values();
    }

    public int size() {
        return accounts.size();
    }
}
