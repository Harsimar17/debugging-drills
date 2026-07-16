package com.acme.settlement.domain;

/**
 * ISO-4217-ish currency with the number of minor units per major unit.
 * Everything in the ledger is stored in minor units (e.g. cents) as a long,
 * so we never lose money to floating point.
 */
public enum Currency {
    USD(100),
    EUR(100),
    GBP(100),
    JPY(1);   // yen has no minor unit

    private final int minorUnitsPerMajor;

    Currency(int minorUnitsPerMajor) {
        this.minorUnitsPerMajor = minorUnitsPerMajor;
    }

    public int minorUnitsPerMajor() {
        return minorUnitsPerMajor;
    }
}
