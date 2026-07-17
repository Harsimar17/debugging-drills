package com.acmelogistics.dispatch.common;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ReferenceNumberGenerator {

    private final AtomicLong sequence = new AtomicLong(1000);

    public String nextOrderNumber() {
        return Constants.ORDER_NUMBER_PREFIX + sequence.incrementAndGet();
    }

    public String nextTrackingNumber(String carrierCode) {
        int suffix = ThreadLocalRandom.current().nextInt(100000, 999999);
        return Constants.TRACKING_NUMBER_PREFIX + carrierCode + "-" + suffix;
    }
}
