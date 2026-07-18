package com.claimsflow.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "adjusters")
public class Adjuster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String employeeCode;

    @Column(nullable = false)
    private String region;

    private int maxActiveClaims = 25;

    @OneToMany(mappedBy = "assignedAdjuster", fetch = FetchType.LAZY)
    private List<Claim> assignedClaims = new ArrayList<>();

    public Adjuster() {
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

    public List<Claim> getAssignedClaims() {
        return assignedClaims;
    }

    public void setAssignedClaims(List<Claim> assignedClaims) {
        this.assignedClaims = assignedClaims;
    }
}
