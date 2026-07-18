package com.medlink.clinic.service.mapper;

import com.medlink.clinic.domain.entity.TimeSlot;
import com.medlink.clinic.service.dto.TimeSlotDto;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotMapper {

    public TimeSlotDto toDto(TimeSlot slot) {
        if (slot == null) {
            return null;
        }
        TimeSlotDto dto = new TimeSlotDto();
        dto.setId(slot.getId());
        dto.setProviderId(slot.getProvider() != null ? slot.getProvider().getId() : null);
        dto.setSlotDate(slot.getSlotDate());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setStatus(slot.getStatus());
        return dto;
    }
}
