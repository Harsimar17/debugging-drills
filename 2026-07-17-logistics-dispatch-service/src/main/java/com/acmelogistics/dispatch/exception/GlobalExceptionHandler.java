package com.acmelogistics.dispatch.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleShipmentNotFound(ShipmentNotFoundException ex) {
        log.warn("Shipment not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CarrierIntegrationException.class)
    public ResponseEntity<ErrorResponse> handleCarrierIntegration(CarrierIntegrationException ex) {
        log.error("Carrier integration failure: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_GATEWAY, "Carrier service is currently unavailable");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception ex) {
        log.warn("Validation failure: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid request payload");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
