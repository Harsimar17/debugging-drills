package com.acme.billing.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyUtilsTest {

    @Test
    void roundsToTwoDecimalPlaces() {
        assertThat(MoneyUtils.round(new BigDecimal("9.999"))).isEqualByComparingTo("10.00");
    }

    @Test
    void appliesPercentageDiscount() {
        BigDecimal discounted = MoneyUtils.applyPercentageDiscount(new BigDecimal("100.00"), new BigDecimal("10"));
        assertThat(discounted).isEqualByComparingTo("90.00");
    }
}
