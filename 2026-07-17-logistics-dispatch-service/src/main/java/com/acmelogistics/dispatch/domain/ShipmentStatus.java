package com.acmelogistics.dispatch.domain;

public enum ShipmentStatus {
    PENDING,
    CARRIER_ASSIGNED,
    DISPATCHED,
    PENDING_RETRY,
    FAILED,
    DELIVERED
}
