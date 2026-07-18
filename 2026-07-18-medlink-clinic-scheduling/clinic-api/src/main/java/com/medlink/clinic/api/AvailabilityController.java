package com.medlink.clinic.api;

import com.medlink.clinic.domain.entity.TimeSlot;
import com.medlink.clinic.service.ProviderAvailabilityCacheService;
import com.medlink.clinic.service.ProviderService;
import com.medlink.clinic.service.dto.AvailabilityResponseDto;
import com.medlink.clinic.service.dto.ProviderDto;
import com.medlink.clinic.service.dto.TimeSlotDto;
import com.medlink.clinic.service.mapper.TimeSlotMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/providers/{providerId}/availability")
public class AvailabilityController {

    private final ProviderAvailabilityCacheService availabilityCacheService;
    private final ProviderService providerService;
    private final TimeSlotMapper timeSlotMapper;

    public AvailabilityController(ProviderAvailabilityCacheService availabilityCacheService,
                                   ProviderService providerService,
                                   TimeSlotMapper timeSlotMapper) {
        this.availabilityCacheService = availabilityCacheService;
        this.providerService = providerService;
        this.timeSlotMapper = timeSlotMapper;
    }

    @GetMapping
    public AvailabilityResponseDto getAvailability(
            @PathVariable Long providerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        ProviderDto provider = providerService.getById(providerId);
        List<TimeSlot> slots = availabilityCacheService.getAvailableSlots(providerId, date);
        List<TimeSlotDto> slotDtos = slots.stream().map(timeSlotMapper::toDto).toList();

        return AvailabilityResponseDto.builder()
                .providerId(providerId)
                .providerName(provider.getFirstName() + " " + provider.getLastName())
                .date(date)
                .availableSlots(slotDtos)
                .build();
    }
}
