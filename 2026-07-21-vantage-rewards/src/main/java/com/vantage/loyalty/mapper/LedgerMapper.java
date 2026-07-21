package com.vantage.loyalty.mapper;

import com.vantage.loyalty.domain.PointsLedgerEntry;
import com.vantage.loyalty.dto.LedgerEntryDto;
import org.springframework.stereotype.Component;

@Component
public class LedgerMapper {

    public LedgerEntryDto toDto(PointsLedgerEntry entry) {
        LedgerEntryDto dto = new LedgerEntryDto();
        dto.setId(entry.getId());
        dto.setEntryType(entry.getEntryType());
        dto.setPoints(entry.getPoints());
        dto.setDescription(entry.getDescription());
        dto.setEarnedAt(entry.getEarnedAt());
        dto.setExpiresAt(entry.getExpiresAt());
        dto.setExpired(entry.isExpired());
        return dto;
    }
}
