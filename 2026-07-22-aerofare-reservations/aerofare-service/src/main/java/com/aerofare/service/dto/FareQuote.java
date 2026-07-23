package com.aerofare.service.dto;

import java.math.BigDecimal;

/**
 * Immutable fare breakdown for a party on a given flight/cabin.
 */
public class FareQuote {

    private final String currency;
    private final BigDecimal baseAmount;
    private final BigDecimal taxAmount;
    private final BigDecimal feeAmount;
    private final BigDecimal totalAmount;

    public FareQuote(String currency, BigDecimal baseAmount, BigDecimal taxAmount,
                     BigDecimal feeAmount, BigDecimal totalAmount) {
        this.currency = currency;
        this.baseAmount = baseAmount;
        this.taxAmount = taxAmount;
        this.feeAmount = feeAmount;
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}
