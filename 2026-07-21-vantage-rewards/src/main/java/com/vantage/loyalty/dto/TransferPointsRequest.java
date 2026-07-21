package com.vantage.loyalty.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class TransferPointsRequest {

    @NotBlank
    private String toMemberNumber;

    @Min(1)
    private long points;

    private String note;

    public String getToMemberNumber() {
        return toMemberNumber;
    }

    public void setToMemberNumber(String toMemberNumber) {
        this.toMemberNumber = toMemberNumber;
    }

    public long getPoints() {
        return points;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
