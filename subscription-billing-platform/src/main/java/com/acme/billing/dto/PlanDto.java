package com.acme.billing.dto;

import com.acme.billing.domain.BillingCycle;

import java.math.BigDecimal;

public class PlanDto {

    private Long id;
    private String code;
    private String name;
    private BigDecimal price;
    private BillingCycle billingCycle;
    private boolean active;

    public PlanDto() {
    }

    public PlanDto(Long id, String code, String name, BigDecimal price, BillingCycle billingCycle, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.price = price;
        this.billingCycle = billingCycle;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
