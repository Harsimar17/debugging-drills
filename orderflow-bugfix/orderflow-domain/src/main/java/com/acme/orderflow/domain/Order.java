package com.acme.orderflow.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An immutable order as received from the checkout service.
 *
 * <p>{@code placedAt} is a wall-clock timestamp in the store's local time,
 * formatted as {@code yyyy-MM-dd HH:mm:ss}. It is carried around as a string
 * because that is exactly how it arrives on the upstream Kafka topic; parsing
 * is deferred to the pricing layer where the calendar actually matters.
 */
public final class Order {

    private final String orderId;
    private final String customerId;
    private final CustomerTier tier;
    private final BigDecimal baseAmount;
    private final String placedAt;

    public Order(String orderId,
                 String customerId,
                 CustomerTier tier,
                 BigDecimal baseAmount,
                 String placedAt) {
        this.orderId = Objects.requireNonNull(orderId, "orderId");
        this.customerId = Objects.requireNonNull(customerId, "customerId");
        this.tier = Objects.requireNonNull(tier, "tier");
        this.baseAmount = Objects.requireNonNull(baseAmount, "baseAmount");
        this.placedAt = Objects.requireNonNull(placedAt, "placedAt");
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public CustomerTier getTier() {
        return tier;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public String getPlacedAt() {
        return placedAt;
    }

    @Override
    public String toString() {
        return "Order{" + orderId + ", " + customerId + ", " + tier
                + ", " + baseAmount + ", " + placedAt + '}';
    }
}
