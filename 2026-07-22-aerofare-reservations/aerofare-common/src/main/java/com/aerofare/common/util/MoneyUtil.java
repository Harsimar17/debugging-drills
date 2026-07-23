package com.aerofare.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Money helpers. All monetary amounts in Aerofare are held to 2 decimal places
 * using banker's-style half-up rounding at the boundaries.
 */
public final class MoneyUtil {

    public static final int SCALE = 2;

    private MoneyUtil() {
    }

    public static BigDecimal normalize(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal sum(BigDecimal a, BigDecimal b) {
        return normalize(nz(a).add(nz(b)));
    }

    public static BigDecimal percentage(BigDecimal base, BigDecimal percent) {
        return normalize(nz(base).multiply(nz(percent)).divide(BigDecimal.valueOf(100), SCALE, RoundingMode.HALF_UP));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
