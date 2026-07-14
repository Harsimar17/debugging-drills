package com.acme.orderflow.service;

import com.acme.orderflow.domain.CustomerTier;
import com.acme.orderflow.domain.Order;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Test-data helper used by the nightly revenue-report reconciliation job.
 */
public final class OrderFactory {

    private OrderFactory() {
    }

    /**
     * Builds a batch of {@code n} STANDARD-tier orders, each for exactly
     * {@code 100.00}, all placed at the same weekday timestamp
     * ({@code 2026-07-15 10:00:00}, a Wednesday).
     *
     * <p>Because every order is a weekday, STANDARD-tier, flat 100.00 order,
     * the expected total revenue is deterministically {@code n * 100.00} with
     * no discounts and no weekend surcharge applied.
     */
    public static List<Order> weekdayBatch(int n) {
        List<Order> orders = new ArrayList<>(n);
        BigDecimal amount = new BigDecimal("100.00");
        for (int i = 0; i < n; i++) {
            orders.add(new Order(
                    "ORD-" + i,
                    "CUST-" + (i % 100),
                    CustomerTier.STANDARD,
                    amount,
                    "2026-07-15 10:00:00"));
        }
        return orders;
    }
}
