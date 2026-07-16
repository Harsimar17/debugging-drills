package com.acme.settlement.domain;

import java.util.Objects;

/**
 * Stable identity for a settlement account. Value-based: two AccountIds with the
 * same value are equal and hash the same, so they behave correctly as map keys.
 */
public final class AccountId {

    private final String value;

    public AccountId(String value) {
        this.value = Objects.requireNonNull(value, "account id");
    }

    public static AccountId of(String value) {
        return new AccountId(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AccountId)) return false;
        return value.equals(((AccountId) o).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
