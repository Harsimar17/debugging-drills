package com.acme.settlement.fx;

import com.acme.settlement.domain.Currency;
import com.acme.settlement.domain.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

/**
 * Converts {@link Money} between currencies using a static mid-market rate table.
 * Rates are expressed as "1 unit of FROM = rate units of TO" in major units.
 * Conversion is done in decimal and rounded half-even back to minor units.
 */
public final class FxConverter {

    private final Map<Currency, Map<Currency, BigDecimal>> rates = new EnumMap<>(Currency.class);

    public FxConverter() {
        // A tiny illustrative rate table (mid-market, not live).
        put(Currency.USD, Currency.EUR, "0.92");
        put(Currency.USD, Currency.GBP, "0.79");
        put(Currency.USD, Currency.JPY, "157.0");
        put(Currency.EUR, Currency.GBP, "0.86");
        put(Currency.EUR, Currency.JPY, "170.6");
        put(Currency.GBP, Currency.JPY, "198.3");
    }

    private void put(Currency from, Currency to, String rate) {
        BigDecimal r = new BigDecimal(rate);
        rates.computeIfAbsent(from, k -> new EnumMap<>(Currency.class)).put(to, r);
        rates.computeIfAbsent(to, k -> new EnumMap<>(Currency.class))
             .put(from, BigDecimal.ONE.divide(r, 12, RoundingMode.HALF_EVEN));
    }

    public Money convert(Money amount, Currency target) {
        Currency source = amount.currency();
        if (source == target) {
            return amount;
        }
        BigDecimal rate = rateOf(source, target);

        // amount is in source minor units; go to major, apply rate, back to target minor.
        BigDecimal sourceMajor = BigDecimal.valueOf(amount.minor())
                .divide(BigDecimal.valueOf(source.minorUnitsPerMajor()), 12, RoundingMode.HALF_EVEN);
        BigDecimal targetMajor = sourceMajor.multiply(rate);
        long targetMinor = targetMajor
                .multiply(BigDecimal.valueOf(target.minorUnitsPerMajor()))
                .setScale(0, RoundingMode.HALF_EVEN)
                .longValueExact();

        return Money.ofMinor(targetMinor, target);
    }

    private BigDecimal rateOf(Currency source, Currency target) {
        Map<Currency, BigDecimal> row = rates.get(source);
        if (row == null || !row.containsKey(target)) {
            throw new IllegalArgumentException("No FX rate for " + source + "->" + target);
        }
        return row.get(target);
    }
}
