package com.acmelogistics.dispatch.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shipments",
uniqueConstraints = @UniqueConstraint(columnNames = "idempotency_key"),
indexes = { @Index(name = "idx_shipment_idempotency_key", columnList = "idempotency_key") })
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "carrier_id")
    private Long carrierId;

    @Column(name = "idempotency_key", nullable = false, length = 80)
    private String idempotencyKey;

    @Column(name = "tracking_number", length = 60)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;

    @Column(name = "weight_kg", precision = 8, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost;

    @Column(name = "dispatch_attempts", nullable = false)
    private int dispatchAttempts;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Shipment() {
    }

    private Shipment(Builder builder) {
        this.id = builder.id;
        this.orderId = builder.orderId;
        this.carrierId = builder.carrierId;
        this.idempotencyKey = builder.idempotencyKey;
        this.trackingNumber = builder.trackingNumber;
        this.status = builder.status;
        this.weightKg = builder.weightKg;
        this.shippingCost = builder.shippingCost;
        this.dispatchAttempts = builder.dispatchAttempts;
    }

    public static Builder builder() {
        return new Builder();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) {
            this.status = ShipmentStatus.PENDING;
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

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static final class Builder {
        private Long id;
        private Long orderId;
        private Long carrierId;
        private String idempotencyKey;
        private String trackingNumber;
        private ShipmentStatus status;
        private BigDecimal weightKg;
        private BigDecimal shippingCost;
        private int dispatchAttempts;

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

        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
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

        public Shipment build() {
            return new Shipment(this);
        }
    }
}
