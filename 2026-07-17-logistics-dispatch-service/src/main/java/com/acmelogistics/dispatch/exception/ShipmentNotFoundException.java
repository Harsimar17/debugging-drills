package com.acmelogistics.dispatch.exception;

public class ShipmentNotFoundException extends RuntimeException {
    public ShipmentNotFoundException(Long shipmentId) {
        super("Shipment not found with id: " + shipmentId);
    }

    public ShipmentNotFoundException(String message) {
        super(message);
    }
}
