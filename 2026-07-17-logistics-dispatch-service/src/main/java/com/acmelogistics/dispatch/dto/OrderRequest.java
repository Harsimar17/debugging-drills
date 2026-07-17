package com.acmelogistics.dispatch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class OrderRequest {

    @NotBlank
    private String customerName;

    @NotBlank
    @Email
    private String customerEmail;

    @NotBlank
    private String deliveryAddress;

    @NotNull
    @Positive
    private BigDecimal totalAmount;

    public OrderRequest() {
    }

    public OrderRequest(String customerName, String customerEmail, String deliveryAddress, BigDecimal totalAmount) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.deliveryAddress = deliveryAddress;
        this.totalAmount = totalAmount;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
