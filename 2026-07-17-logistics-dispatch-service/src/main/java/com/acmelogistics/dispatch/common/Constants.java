package com.acmelogistics.dispatch.common;

public final class Constants {

    private Constants() {
    }

    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String ORDER_NUMBER_PREFIX = "ACME-ORD-";
    public static final String TRACKING_NUMBER_PREFIX = "ACME-TRK-";
}
