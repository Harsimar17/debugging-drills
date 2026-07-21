package com.vantage.loyalty;

import com.vantage.loyalty.config.RewardProperties;
import com.vantage.loyalty.domain.enums.MembershipTier;
import com.vantage.loyalty.service.RewardCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RewardCalculatorTest {

    private final RewardCalculator calculator = new RewardCalculator(new RewardProperties());

    @Test
    void standardTierEarnsBasePoints() {
        long points = calculator.pointsForSpend(new BigDecimal("120.00"), MembershipTier.STANDARD);
        assertEquals(120L, points);
    }

    @Test
    void goldTierEarnsBonusPoints() {
        long points = calculator.pointsForSpend(new BigDecimal("100.00"), MembershipTier.GOLD);
        assertEquals(150L, points);
    }

    @Test
    void silverTierAppliesMultiplier() {
        long points = calculator.pointsForSpend(new BigDecimal("200.00"), MembershipTier.SILVER);
        assertEquals(250L, points);
    }

    @Test
    void pointsConvertToCurrencyAtConfiguredRate() {
        BigDecimal value = calculator.pointsToCurrency(900L);
        assertEquals(new BigDecimal("300.00"), value);
    }
}
