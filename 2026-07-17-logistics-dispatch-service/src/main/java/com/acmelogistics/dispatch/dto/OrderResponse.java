package com.acmelogistics.dispatch.dto;

import com.acmelogistics.dispatch.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderResponse {
    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerEmail;
    private String deliveryAddress;
    private OrderStatus orderStatus;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;

    public OrderResponse() {
    }

    private OrderResponse(Builder builder) {
        this.id = builder.id;
        this.orderNumber = builder.orderNumber;
        this.customerName = builder.customerName;
        this.customerEmail = builder.customerEmail;
        this.deliveryAddress = builder.deliveryAddress;
        this.orderStatus = builder.orderStatus;
        this.totalAmount = builder.totalAmount;
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

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static final class Builder {
        private Long id;
        private String orderNumber;
        private String customerName;
        private String customerEmail;
        private String deliveryAddress;
        private OrderStatus orderStatus;
        private BigDecimal totalAmount;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder orderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
            return this;
        }

        public Builder customerName(String customerName) {
            this.customerName = customerName;
            return this;
        }

        public Builder customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }

        public Builder deliveryAddress(String deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder orderStatus(OrderStatus orderStatus) {
            this.orderStatus = orderStatus;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public OrderResponse build() {
            return new OrderResponse(this);
        }
    }
}
