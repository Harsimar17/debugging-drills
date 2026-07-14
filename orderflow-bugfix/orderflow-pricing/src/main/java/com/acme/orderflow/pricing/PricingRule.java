package com.acme.orderflow.pricing;

import com.acme.orderflow.domain.Order;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;

/**
 * A single step in the pricing pipeline. Each rule takes the running price and
 * returns the adjusted price; rules are chained in a fixed order by the engine.
 */
public interface PricingRule {

    BigDecimal apply(Order order, BigDecimal runningPrice, SimpleDateFormat fORMAT);
}
