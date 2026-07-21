package com.vantage.loyalty.dto;

import jakarta.validation.constraints.NotBlank;

public class RedeemRequest {

    @NotBlank
    private String sku;

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }
}
