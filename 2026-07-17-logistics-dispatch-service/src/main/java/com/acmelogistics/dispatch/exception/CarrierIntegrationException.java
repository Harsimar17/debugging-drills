package com.acmelogistics.dispatch.exception;

public class CarrierIntegrationException extends RuntimeException {
    public CarrierIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CarrierIntegrationException(String message) {
        super(message);
    }
}
