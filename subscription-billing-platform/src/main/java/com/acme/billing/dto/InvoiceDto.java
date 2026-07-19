package com.acme.billing.dto;

import com.acme.billing.domain.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class InvoiceDto {

    private Long id;
    private String invoiceNumber;
    private Long subscriptionId;
    private BigDecimal amount;
    private LocalDate billingPeriodStart;
    private LocalDate billingPeriodEnd;
    private InvoiceStatus status;
    private Instant issuedAt;

    public InvoiceDto() {
    }

    public InvoiceDto(Long id, String invoiceNumber, Long subscriptionId, BigDecimal amount,
                       LocalDate billingPeriodStart, LocalDate billingPeriodEnd,
                       InvoiceStatus status, Instant issuedAt) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.billingPeriodStart = billingPeriodStart;
        this.billingPeriodEnd = billingPeriodEnd;
        this.status = status;
        this.issuedAt = issuedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getBillingPeriodStart() {
        return billingPeriodStart;
    }

    public void setBillingPeriodStart(LocalDate billingPeriodStart) {
        this.billingPeriodStart = billingPeriodStart;
    }

    public LocalDate getBillingPeriodEnd() {
        return billingPeriodEnd;
    }

    public void setBillingPeriodEnd(LocalDate billingPeriodEnd) {
        this.billingPeriodEnd = billingPeriodEnd;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }
}
