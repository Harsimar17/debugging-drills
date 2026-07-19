package com.acme.billing.dto;

import java.time.Instant;

public class CustomerDto {

    private Long id;
    private String fullName;
    private String email;
    private String billingAddress;
    private Instant createdAt;

    public CustomerDto() {
    }

    public CustomerDto(Long id, String fullName, String email, String billingAddress, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.billingAddress = billingAddress;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
