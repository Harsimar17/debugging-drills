package com.vantage.loyalty.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@ConfigurationProperties(prefix = "vantage.rewards")
public class RewardProperties {

    private BigDecimal baseEarnRate = BigDecimal.ONE;
    private int redemptionConversionRate = 3;
    private int pointsValidityMonths = 12;
    private int expiryGraceDays = 0;

    public BigDecimal getBaseEarnRate() {
        return baseEarnRate;
    }

    public void setBaseEarnRate(BigDecimal baseEarnRate) {
        this.baseEarnRate = baseEarnRate;
    }

    public int getRedemptionConversionRate() {
        return redemptionConversionRate;
    }

    public void setRedemptionConversionRate(int redemptionConversionRate) {
        this.redemptionConversionRate = redemptionConversionRate;
    }

    public int getPointsValidityMonths() {
        return pointsValidityMonths;
    }

    public void setPointsValidityMonths(int pointsValidityMonths) {
        this.pointsValidityMonths = pointsValidityMonths;
    }

    public int getExpiryGraceDays() {
        return expiryGraceDays;
    }

    public void setExpiryGraceDays(int expiryGraceDays) {
        this.expiryGraceDays = expiryGraceDays;
    }
}
