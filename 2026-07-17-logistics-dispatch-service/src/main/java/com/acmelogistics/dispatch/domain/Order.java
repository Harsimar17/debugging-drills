package com.acmelogistics.dispatch.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", uniqueConstraints = @UniqueConstraint(columnNames = "order_number"))
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, length = 40)
    private String orderNumber;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 160)
    private String customerEmail;

    @Column(name = "delivery_address", nullable = false, length = 250)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Order() {
    }

    private Order(Builder builder) {
        this.id = builder.id;
        this.orderNumber = builder.orderNumber;
        this.customerName = builder.customerName;
        this.customerEmail = builder.customerEmail;
        this.deliveryAddress = builder.deliveryAddress;
        this.orderStatus = builder.orderStatus;
        this.totalAmount = builder.totalAmount;
    }

    public static Builder builder() {
        return new Builder();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.orderStatus == null) {
            this.orderStatus = OrderStatus.RECEIVED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static final class Builder {
        private Long id;
        private String orderNumber;
        private String customerName;
        private String customerEmail;
        private String deliveryAddress;
        private OrderStatus orderStatus;
        private BigDecimal totalAmount;

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

        public Order build() {
            return new Order(this);
        }
    }
}
