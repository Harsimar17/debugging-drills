package com.acmelogistics.dispatch.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "dispatch_events")
public class DispatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private DispatchEventType eventType;

    @Column(length = 500)
    private String details;

    @Column(name = "event_timestamp", nullable = false, updatable = false)
    private LocalDateTime eventTimestamp;

    public DispatchEvent() {
    }

    private DispatchEvent(Builder builder) {
        this.id = builder.id;
        this.shipmentId = builder.shipmentId;
        this.eventType = builder.eventType;
        this.details = builder.details;
    }

    public static Builder builder() {
        return new Builder();
    }

    @PrePersist
    protected void onCreate() {
        this.eventTimestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(Long shipmentId) {
        this.shipmentId = shipmentId;
    }

    public DispatchEventType getEventType() {
        return eventType;
    }

    public void setEventType(DispatchEventType eventType) {
        this.eventType = eventType;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public static final class Builder {
        private Long id;
        private Long shipmentId;
        private DispatchEventType eventType;
        private String details;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder shipmentId(Long shipmentId) {
            this.shipmentId = shipmentId;
            return this;
        }

        public Builder eventType(DispatchEventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder details(String details) {
            this.details = details;
            return this;
        }

        public DispatchEvent build() {
            return new DispatchEvent(this);
        }
    }
}
