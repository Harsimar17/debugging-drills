package com.medlink.clinic.service.mapper;

import com.medlink.clinic.domain.entity.Provider;
import com.medlink.clinic.service.dto.ProviderDto;
import org.springframework.stereotype.Component;

@Component
public class ProviderMapper {

    public ProviderDto toDto(Provider provider) {
        if (provider == null) {
            return null;
        }
        ProviderDto dto = new ProviderDto();
        dto.setId(provider.getId());
        dto.setFirstName(provider.getFirstName());
        dto.setLastName(provider.getLastName());
        dto.setSpecialty(provider.getSpecialty());
        dto.setNpiNumber(provider.getNpiNumber());
        return dto;
    }

    public Provider toEntity(ProviderDto dto) {
        if (dto == null) {
            return null;
        }
        Provider provider = new Provider();
        provider.setId(dto.getId());
        provider.setFirstName(dto.getFirstName());
        provider.setLastName(dto.getLastName());
        provider.setSpecialty(dto.getSpecialty());
        provider.setNpiNumber(dto.getNpiNumber());
        return provider;
    }
}
