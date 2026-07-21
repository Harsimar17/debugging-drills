package com.vantage.loyalty.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class EarnPointsRequest {

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal spendAmount;

    /**
     * Business reference for the spend (e.g. order id). Used to keep earning
     * idempotent so the same order is never credited twice.
     */
    @NotNull
    private String sourceReference;

    private String description;

    public BigDecimal getSpendAmount() {
        return spendAmount;
    }

    public void setSpendAmount(BigDecimal spendAmount) {
        this.spendAmount = spendAmount;
    }

    public String getSourceReference() {
        return sourceReference;
    }

    public void setSourceReference(String sourceReference) {
        this.sourceReference = sourceReference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
