package com.aerofare.service;

import com.aerofare.common.util.MoneyUtil;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyUtilTest {

    @Test
    void normalizeScalesToTwoDecimals() {
        assertEquals(new BigDecimal("10.00"), MoneyUtil.normalize(new BigDecimal("10")));
        assertEquals(new BigDecimal("10.13"), MoneyUtil.normalize(new BigDecimal("10.125")));
    }

    @Test
    void percentageComputesShare() {
        assertEquals(new BigDecimal("12.00"), MoneyUtil.percentage(new BigDecimal("100.00"), new BigDecimal("12")));
    }

    @Test
    void sumHandlesNulls() {
        assertEquals(new BigDecimal("5.00"), MoneyUtil.sum(null, new BigDecimal("5")));
    }
}
