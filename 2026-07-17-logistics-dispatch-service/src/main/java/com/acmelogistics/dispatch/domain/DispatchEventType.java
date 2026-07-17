package com.acmelogistics.dispatch.domain;

public enum DispatchEventType {
    SHIPMENT_CREATED,
    CARRIER_QUOTE_RECEIVED,
    CARRIER_NOTIFIED,
    RETRY_SCHEDULED,
    DISPATCH_FAILED,
    DELIVERY_CONFIRMED
}
