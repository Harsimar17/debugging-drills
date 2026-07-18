package com.medlink.clinic.service.dto;

import java.time.LocalDate;
import java.util.List;

public class AvailabilityResponseDto {

    private Long providerId;
    private String providerName;
    private LocalDate date;
    private List<TimeSlotDto> availableSlots;

    public AvailabilityResponseDto() {
    }

    public AvailabilityResponseDto(Long providerId, String providerName, LocalDate date, List<TimeSlotDto> availableSlots) {
        this.providerId = providerId;
        this.providerName = providerName;
        this.date = date;
        this.availableSlots = availableSlots;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getProviderId() {
        return providerId;
    }

    public void setProviderId(Long providerId) {
        this.providerId = providerId;
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

    public List<TimeSlotDto> getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(List<TimeSlotDto> availableSlots) {
        this.availableSlots = availableSlots;
    }

    public static final class Builder {
        private Long providerId;
        private String providerName;
        private LocalDate date;
        private List<TimeSlotDto> availableSlots;

        public Builder providerId(Long providerId) {
            this.providerId = providerId;
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

        public Builder availableSlots(List<TimeSlotDto> availableSlots) {
            this.availableSlots = availableSlots;
            return this;
        }

        public AvailabilityResponseDto build() {
            return new AvailabilityResponseDto(providerId, providerName, date, availableSlots);
        }
    }
}
