package com.aerofare.service.refund;

import java.math.BigDecimal;

public class RefundResult {

    private String recordLocator;
    private BigDecimal originalAmount;
    private BigDecimal cancellationFee;
    private BigDecimal refundedAmount;
    private String status;

    public RefundResult() {
    }

    public RefundResult(String recordLocator, BigDecimal originalAmount, BigDecimal cancellationFee,
                        BigDecimal refundedAmount, String status) {
        this.recordLocator = recordLocator;
        this.originalAmount = originalAmount;
        this.cancellationFee = cancellationFee;
        this.refundedAmount = refundedAmount;
        this.status = status;
    }

    public String getRecordLocator() {
        return recordLocator;
    }

    public void setRecordLocator(String recordLocator) {
        this.recordLocator = recordLocator;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getCancellationFee() {
        return cancellationFee;
    }

    public void setCancellationFee(BigDecimal cancellationFee) {
        this.cancellationFee = cancellationFee;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
