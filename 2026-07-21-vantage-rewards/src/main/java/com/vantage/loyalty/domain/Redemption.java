package com.vantage.loyalty.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "redemption")
public class Redemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "sku", nullable = false, length = 40)
    private String sku;

    @Column(name = "points_spent", nullable = false)
    private long pointsSpent;

    @Column(name = "cash_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal cashValue;

    @Column(name = "redeemed_at", nullable = false)
    private LocalDateTime redeemedAt;

    protected Redemption() {
    }

    public Redemption(Long memberId, String sku, long pointsSpent, BigDecimal cashValue) {
        this.memberId = memberId;
        this.sku = sku;
        this.pointsSpent = pointsSpent;
        this.cashValue = cashValue;
        this.redeemedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getSku() {
        return sku;
    }

    public long getPointsSpent() {
        return pointsSpent;
    }

    public BigDecimal getCashValue() {
        return cashValue;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }
}
