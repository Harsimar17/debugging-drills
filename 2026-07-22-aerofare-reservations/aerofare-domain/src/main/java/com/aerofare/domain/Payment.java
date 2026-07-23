package com.aerofare.domain;

import com.aerofare.common.enums.PaymentMethod;
import com.aerofare.common.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment",
        indexes = @Index(name = "idx_payment_booking", columnList = "booking_id"))
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "transaction_ref", nullable = false, length = 40)
    private String transactionRef;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    protected Payment() {
    }

    public Payment(Long bookingId, BigDecimal amount, PaymentMethod method, PaymentStatus status,
                   String transactionRef) {
        this.bookingId = bookingId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.transactionRef = transactionRef;
        this.processedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getTransactionRef() {
        return transactionRef;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }
}
