package com.vantage.loyalty.dto;

import com.vantage.loyalty.domain.enums.MembershipTier;

import java.math.BigDecimal;

public class AccountSummaryDto {

    private Long memberId;
    private String memberNumber;
    private String fullName;
    private MembershipTier tier;
    private long availablePoints;
    private BigDecimal estimatedCashValue;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public MembershipTier getTier() {
        return tier;
    }

    public void setTier(MembershipTier tier) {
        this.tier = tier;
    }

    public long getAvailablePoints() {
        return availablePoints;
    }

    public void setAvailablePoints(long availablePoints) {
        this.availablePoints = availablePoints;
    }

    public BigDecimal getEstimatedCashValue() {
        return estimatedCashValue;
    }

    public void setEstimatedCashValue(BigDecimal estimatedCashValue) {
        this.estimatedCashValue = estimatedCashValue;
    }
}
