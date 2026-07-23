package com.aerofare.domain;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.common.enums.PassengerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;

/**
 * Pricing and policy rule for a (cabin, passenger-type) combination. The base
 * flight fare is multiplied by {@code fareMultiplier}; refund/change policy is
 * expressed as a percentage of the ticket value.
 */
@Entity
@Table(name = "fare_rule",
        uniqueConstraints = @UniqueConstraint(columnNames = {"cabin_class", "passenger_type"}))
public class FareRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "cabin_class", nullable = false, length = 20)
    private CabinClass cabinClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "passenger_type", nullable = false, length = 20)
    private PassengerType passengerType;

    @Column(name = "fare_multiplier", nullable = false, precision = 6, scale = 3)
    private BigDecimal fareMultiplier;

    @Column(name = "refundable", nullable = false)
    private boolean refundable;

    @Column(name = "change_fee_percent", nullable = false, precision = 6, scale = 2)
    private BigDecimal changeFeePercent;

    @Column(name = "cancellation_fee_percent", nullable = false, precision = 6, scale = 2)
    private BigDecimal cancellationFeePercent;

    protected FareRule() {
    }

    public FareRule(CabinClass cabinClass, PassengerType passengerType, BigDecimal fareMultiplier,
                    boolean refundable, BigDecimal changeFeePercent, BigDecimal cancellationFeePercent) {
        this.cabinClass = cabinClass;
        this.passengerType = passengerType;
        this.fareMultiplier = fareMultiplier;
        this.refundable = refundable;
        this.changeFeePercent = changeFeePercent;
        this.cancellationFeePercent = cancellationFeePercent;
    }

    public Long getId() {
        return id;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public PassengerType getPassengerType() {
        return passengerType;
    }

    public BigDecimal getFareMultiplier() {
        return fareMultiplier;
    }

    public boolean isRefundable() {
        return refundable;
    }

    public BigDecimal getChangeFeePercent() {
        return changeFeePercent;
    }

    public BigDecimal getCancellationFeePercent() {
        return cancellationFeePercent;
    }
}
