package com.claimsflow.dto;

import com.claimsflow.domain.enums.ClaimStatus;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ClaimStatusUpdateDto {

    @NotNull
    private ClaimStatus newStatus;

    private BigDecimal approvedAmount;

    private String notes;

    private String changedBy;

    public ClaimStatusUpdateDto() {
    }

    public ClaimStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ClaimStatus newStatus) {
        this.newStatus = newStatus;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }
}
