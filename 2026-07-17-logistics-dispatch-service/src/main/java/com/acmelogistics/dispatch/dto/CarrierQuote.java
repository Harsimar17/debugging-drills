package com.acmelogistics.dispatch.dto;

import java.math.BigDecimal;

public class CarrierQuote {
    private Long carrierId;
    private String carrierCode;
    private BigDecimal cost;
    private int estimatedDeliveryDays;

    public CarrierQuote() {
    }

    private CarrierQuote(Builder builder) {
        this.carrierId = builder.carrierId;
        this.carrierCode = builder.carrierCode;
        this.cost = builder.cost;
        this.estimatedDeliveryDays = builder.estimatedDeliveryDays;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(Long carrierId) {
        this.carrierId = carrierId;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public int getEstimatedDeliveryDays() {
        return estimatedDeliveryDays;
    }

    public void setEstimatedDeliveryDays(int estimatedDeliveryDays) {
        this.estimatedDeliveryDays = estimatedDeliveryDays;
    }

    public static final class Builder {
        private Long carrierId;
        private String carrierCode;
        private BigDecimal cost;
        private int estimatedDeliveryDays;

        public Builder carrierId(Long carrierId) {
            this.carrierId = carrierId;
            return this;
        }

        public Builder carrierCode(String carrierCode) {
            this.carrierCode = carrierCode;
            return this;
        }

        public Builder cost(BigDecimal cost) {
            this.cost = cost;
            return this;
        }

        public Builder estimatedDeliveryDays(int estimatedDeliveryDays) {
            this.estimatedDeliveryDays = estimatedDeliveryDays;
            return this;
        }

        public CarrierQuote build() {
            return new CarrierQuote(this);
        }
    }
}
