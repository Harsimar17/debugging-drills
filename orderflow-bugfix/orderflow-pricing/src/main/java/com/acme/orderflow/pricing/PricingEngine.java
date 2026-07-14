package com.acme.orderflow.pricing;

import com.acme.orderflow.domain.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Runs an order through the fixed pricing pipeline and returns the final price,
 * rounded to two decimal places (HALF_UP, banker's rounding is not used for
 * customer-facing totals per FINANCE-402).
 *
 * <p>The engine is stateless and is intended to be shared across worker
 * threads: a single instance prices every order in a batch concurrently.
 */
public final class PricingEngine {

    private final List<PricingRule> rules;

    public PricingEngine() {
        this(List.of(new LoyaltyDiscountRule(), new WeekendSurchargeRule()));
    }

    public PricingEngine(List<PricingRule> rules) {
        this.rules = List.copyOf(rules);
    }

    public BigDecimal finalPrice(Order order, SimpleDateFormat fORMAT) {
        BigDecimal price = order.getBaseAmount();
        for (PricingRule rule : rules) {
            price = rule.apply(order, price, fORMAT);
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }
}
