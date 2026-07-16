package com.acme.settlement.domain;

/**
 * A settlement account for a single counterparty in a single currency.
 *
 * The balance is mutated only through {@link #credit} / {@link #debit}, both of
 * which are synchronized on the account instance, so an individual account is
 * safe to update from multiple settlement threads.
 */
public final class Account {

    private final AccountId id;
    private final Currency currency;
    private long balanceMinor;

    public Account(AccountId id, Currency currency) {
        this.id = id;
        this.currency = currency;
        this.balanceMinor = 0L;
    }

    public AccountId id() {
        return id;
    }

    public Currency currency() {
        return currency;
    }

    public synchronized void credit(Money amount) {
        checkCurrency(amount);
        this.balanceMinor += amount.minor();
    }

    public synchronized void debit(Money amount) {
        checkCurrency(amount);
        this.balanceMinor -= amount.minor();
    }

    public synchronized Money balance() {
        return Money.ofMinor(balanceMinor, currency);
    }

    private void checkCurrency(Money amount) {
        if (amount.currency() != currency) {
            throw new IllegalArgumentException(
                    "Account " + id + " is " + currency + " but got " + amount.currency());
        }
    }

    @Override
    public String toString() {
        return "Account{" + id + ", " + balance() + "}";
    }
}
