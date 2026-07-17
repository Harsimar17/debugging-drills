package com.acmelogistics.dispatch.domain;

public enum OrderStatus {
    RECEIVED,
    AWAITING_DISPATCH,
    DISPATCHED,
    IN_TRANSIT,
    DELIVERED,
    CANCELLED
}
