package com.acme.settlement.domain;

import java.util.Objects;

/**
 * Immutable money value object. Amount is held in minor units (cents, pence, ...)
 * to keep the ledger exact.
 */
public final class Money {

    private final long minor;
    private final Currency currency;

    private Money(long minor, Currency currency) {
        this.minor = minor;
        this.currency = currency;
    }

    public static Money ofMinor(long minor, Currency currency) {
        return new Money(minor, currency);
    }

    public static Money ofMajor(long major, Currency currency) {
        return new Money(major * currency.minorUnitsPerMajor(), currency);
    }

    public static Money zero(Currency currency) {
        return new Money(0, currency);
    }

    public long minor() {
        return minor;
    }

    public Currency currency() {
        return currency;
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(this.minor + other.minor, currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        return new Money(this.minor - other.minor, currency);
    }

    private void requireSameCurrency(Money other) {
        if (this.currency != other.currency) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        Money money = (Money) o;
        return minor == money.minor && currency == money.currency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minor, currency);
    }

    @Override
    public String toString() {
        return String.format("%d.%02d %s",
                minor / currency.minorUnitsPerMajor(),
                Math.abs(minor % currency.minorUnitsPerMajor()),
                currency);
    }
}
