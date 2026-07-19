package com.acme.billing.dto;

import com.acme.billing.domain.SubscriptionStatus;

import java.time.LocalDate;

public class SubscriptionDto {

    private Long id;
    private Long customerId;
    private String customerName;
    private String planCode;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate nextBillingDate;

    public SubscriptionDto() {
    }

    public SubscriptionDto(Long id, Long customerId, String customerName, String planCode,
                            SubscriptionStatus status, LocalDate startDate, LocalDate nextBillingDate) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.planCode = planCode;
        this.status = status;
        this.startDate = startDate;
        this.nextBillingDate = nextBillingDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getNextBillingDate() {
        return nextBillingDate;
    }

    public void setNextBillingDate(LocalDate nextBillingDate) {
        this.nextBillingDate = nextBillingDate;
    }
}
