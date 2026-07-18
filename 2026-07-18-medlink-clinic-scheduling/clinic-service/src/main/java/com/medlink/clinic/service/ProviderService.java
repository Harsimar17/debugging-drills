package com.medlink.clinic.service;

import com.medlink.clinic.common.exception.ResourceNotFoundException;
import com.medlink.clinic.domain.entity.Provider;
import com.medlink.clinic.repository.ProviderRepository;
import com.medlink.clinic.service.dto.ProviderDto;
import com.medlink.clinic.service.mapper.ProviderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;

    public ProviderService(ProviderRepository providerRepository, ProviderMapper providerMapper) {
        this.providerRepository = providerRepository;
        this.providerMapper = providerMapper;
    }

    @Transactional(readOnly = true)
    public ProviderDto getById(Long id) {
        Provider provider = providerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + id));
        return providerMapper.toDto(provider);
    }

    @Transactional(readOnly = true)
    public List<ProviderDto> getBySpecialty(String specialty) {
        return providerRepository.findBySpecialtyIgnoreCase(specialty).stream()
                .map(providerMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProviderDto> getAll() {
        return providerRepository.findAll().stream()
                .map(providerMapper::toDto)
                .toList();
    }
}
