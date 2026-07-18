package com.medlink.clinic.service.dto;

import java.time.LocalDate;

public class AppointmentRequestDto {

    private Long patientId;
    private Long providerId;
    private Long slotId;
    private LocalDate date;

    public AppointmentRequestDto() {
    }

    public AppointmentRequestDto(Long patientId, Long providerId, Long slotId, LocalDate date) {
        this.patientId = patientId;
        this.providerId = providerId;
        this.slotId = slotId;
        this.date = date;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
