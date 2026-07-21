package com.vantage.loyalty.dto;

import com.vantage.loyalty.domain.enums.MembershipTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class EnrollMemberRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    private MembershipTier tier = MembershipTier.STANDARD;

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

    public MembershipTier getTier() {
        return tier;
    }

    public void setTier(MembershipTier tier) {
        this.tier = tier;
    }
}
