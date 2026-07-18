package com.claimsflow.dto;

public class AdjusterDto {

    private Long id;
    private String fullName;
    private String employeeCode;
    private String region;
    private int maxActiveClaims;
    private int currentActiveClaims;

    public AdjusterDto() {
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

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getMaxActiveClaims() {
        return maxActiveClaims;
    }

    public void setMaxActiveClaims(int maxActiveClaims) {
        this.maxActiveClaims = maxActiveClaims;
    }

    public int getCurrentActiveClaims() {
        return currentActiveClaims;
    }

    public void setCurrentActiveClaims(int currentActiveClaims) {
        this.currentActiveClaims = currentActiveClaims;
    }
}
