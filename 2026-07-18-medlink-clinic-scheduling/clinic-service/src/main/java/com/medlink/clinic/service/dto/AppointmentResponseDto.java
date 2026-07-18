package com.medlink.clinic.service.dto;

import com.medlink.clinic.domain.enums.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentResponseDto {

    private Long id;
    private String confirmationCode;
    private String patientName;
    private String providerName;
    private LocalDate date;
    private LocalTime startTime;
    private AppointmentStatus status;

    public AppointmentResponseDto() {
    }

    public AppointmentResponseDto(Long id, String confirmationCode, String patientName, String providerName,
                                   LocalDate date, LocalTime startTime, AppointmentStatus status) {
        this.id = id;
        this.confirmationCode = confirmationCode;
        this.patientName = patientName;
        this.providerName = providerName;
        this.date = date;
        this.startTime = startTime;
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public static final class Builder {
        private Long id;
        private String confirmationCode;
        private String patientName;
        private String providerName;
        private LocalDate date;
        private LocalTime startTime;
        private AppointmentStatus status;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder confirmationCode(String confirmationCode) {
            this.confirmationCode = confirmationCode;
            return this;
        }

        public Builder patientName(String patientName) {
            this.patientName = patientName;
            return this;
        }

        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public Builder date(LocalDate date) {
            this.date = date;
            return this;
        }

        public Builder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder status(AppointmentStatus status) {
            this.status = status;
            return this;
        }

        public AppointmentResponseDto build() {
            return new AppointmentResponseDto(id, confirmationCode, patientName, providerName, date, startTime, status);
        }
    }
}
