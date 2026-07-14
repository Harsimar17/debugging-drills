package com.acme.orderflow.pricing;

import com.acme.orderflow.domain.Order;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/**
 * Applies the loyalty discount: GOLD customers pay 90%, SILVER customers 95%,
 * STANDARD customers the full price.
 */
public final class LoyaltyDiscountRule implements PricingRule {

    private static final BigDecimal GOLD_FACTOR = new BigDecimal("0.90");
    private static final BigDecimal SILVER_FACTOR = new BigDecimal("0.95");

    @Override
    public BigDecimal apply(Order order, BigDecimal runningPrice, SimpleDateFormat FORMAT) {
        switch (order.getTier()) {
            case GOLD:
                return runningPrice.multiply(GOLD_FACTOR);
            case SILVER:
                return runningPrice.multiply(SILVER_FACTOR);
            default:
                return runningPrice;
        }
    }
}
