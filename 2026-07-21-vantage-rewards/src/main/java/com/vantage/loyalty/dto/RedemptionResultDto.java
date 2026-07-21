package com.vantage.loyalty.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RedemptionResultDto {

    private Long redemptionId;
    private String sku;
    private long pointsSpent;
    private BigDecimal cashValue;
    private long remainingBalance;
    private LocalDateTime redeemedAt;

    public Long getRedemptionId() {
        return redemptionId;
    }

    public void setRedemptionId(Long redemptionId) {
        this.redemptionId = redemptionId;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public long getPointsSpent() {
        return pointsSpent;
    }

    public void setPointsSpent(long pointsSpent) {
        this.pointsSpent = pointsSpent;
    }

    public BigDecimal getCashValue() {
        return cashValue;
    }

    public void setCashValue(BigDecimal cashValue) {
        this.cashValue = cashValue;
    }

    public long getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(long remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(LocalDateTime redeemedAt) {
        this.redeemedAt = redeemedAt;
    }
}
