package com.acmelogistics.dispatch.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ShipmentRequest {

    @NotNull
    private Long orderId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal weightKg;

    private Long preferredCarrierId;

    public ShipmentRequest() {
    }

    public ShipmentRequest(Long orderId, BigDecimal weightKg, Long preferredCarrierId) {
        this.orderId = orderId;
        this.weightKg = weightKg;
        this.preferredCarrierId = preferredCarrierId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public Long getPreferredCarrierId() {
        return preferredCarrierId;
    }

    public void setPreferredCarrierId(Long preferredCarrierId) {
        this.preferredCarrierId = preferredCarrierId;
    }
}
