package com.acme.orderflow.pricing;

import com.acme.orderflow.domain.Order;
import com.acme.orderflow.domain.TimestampParser;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * Adds a 15% surcharge to orders placed on a weekend (Saturday or Sunday),
 * matching the weekend delivery premium negotiated with the logistics partner.
 */
public final class WeekendSurchargeRule implements PricingRule {

    private static final BigDecimal WEEKEND_FACTOR = new BigDecimal("1.15");

    @Override
    public BigDecimal apply(Order order, BigDecimal runningPrice, SimpleDateFormat fORMAT) {
        LocalDate placed = TimestampParser.toLocalDate(order.getPlacedAt(), fORMAT);
        DayOfWeek day = placed.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return runningPrice.multiply(WEEKEND_FACTOR);
        }
        return runningPrice;
    }
}
