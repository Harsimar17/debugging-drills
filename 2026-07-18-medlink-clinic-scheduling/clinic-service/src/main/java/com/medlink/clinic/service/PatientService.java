package com.medlink.clinic.service;

import com.medlink.clinic.common.exception.ResourceNotFoundException;
import com.medlink.clinic.domain.entity.Patient;
import com.medlink.clinic.repository.PatientRepository;
import com.medlink.clinic.service.dto.PatientDto;
import com.medlink.clinic.service.mapper.PatientMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    @Transactional
    public PatientDto register(PatientDto dto) {
        Patient patient = patientMapper.toEntity(dto);
        patient.setId(null);
        Patient saved = patientRepository.save(patient);
        log.info("Registered new patient id={} email={}", saved.getId(), saved.getEmail());
        return patientMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public PatientDto getById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
        return patientMapper.toDto(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientDto> getAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDto)
                .toList();
    }
}
