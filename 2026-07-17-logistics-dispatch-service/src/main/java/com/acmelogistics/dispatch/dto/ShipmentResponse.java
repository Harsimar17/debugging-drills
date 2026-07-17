package com.acmelogistics.dispatch.dto;

import com.acmelogistics.dispatch.domain.ShipmentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ShipmentResponse {
    private Long id;
    private Long orderId;
    private Long carrierId;
    private String trackingNumber;
    private ShipmentStatus status;
    private BigDecimal weightKg;
    private BigDecimal shippingCost;
    private int dispatchAttempts;
    private LocalDateTime createdAt;

    public ShipmentResponse() {
    }

    private ShipmentResponse(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.carrierId = builder.carrierId;
        this.trackingNumber = builder.trackingNumber;
        this.status = builder.status;
        this.weightKg = builder.weightKg;
        this.shippingCost = builder.shippingCost;
        this.dispatchAttempts = builder.dispatchAttempts;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void setStatus(ShipmentStatus status) {
        this.status = status;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public BigDecimal getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(BigDecimal shippingCost) {
        this.shippingCost = shippingCost;
    }

    public int getDispatchAttempts() {
        return dispatchAttempts;
    }

    public void setDispatchAttempts(int dispatchAttempts) {
        this.dispatchAttempts = dispatchAttempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static final class Builder {
        private Long id;
        private Long orderId;
        private Long carrierId;
        private String trackingNumber;
        private ShipmentStatus status;
        private BigDecimal weightKg;
        private BigDecimal shippingCost;
        private int dispatchAttempts;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderId(Long orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder carrierId(Long carrierId) {
            this.carrierId = carrierId;
            return this;
        }

        public Builder trackingNumber(String trackingNumber) {
            this.trackingNumber = trackingNumber;
            return this;
        }

        public Builder status(ShipmentStatus status) {
            this.status = status;
            return this;
        }

        public Builder weightKg(BigDecimal weightKg) {
            this.weightKg = weightKg;
            return this;
        }

        public Builder shippingCost(BigDecimal shippingCost) {
            this.shippingCost = shippingCost;
            return this;
        }

        public Builder dispatchAttempts(int dispatchAttempts) {
            this.dispatchAttempts = dispatchAttempts;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ShipmentResponse build() {
            return new ShipmentResponse(this);
        }
    }
}
