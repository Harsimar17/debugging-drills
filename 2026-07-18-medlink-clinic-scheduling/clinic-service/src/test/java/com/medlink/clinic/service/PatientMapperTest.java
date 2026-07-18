package com.medlink.clinic.service;

import com.medlink.clinic.domain.entity.Patient;
import com.medlink.clinic.service.dto.PatientDto;
import com.medlink.clinic.service.mapper.PatientMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PatientMapperTest {

    private final PatientMapper mapper = new PatientMapper();

    @Test
    void mapsEntityToDto() {
        Patient patient = new Patient();
        patient.setId(1L);
        patient.setFirstName("Jane");
        patient.setLastName("Doe");
        patient.setEmail("jane.doe@example.com");
        patient.setPhone("555-0100");
        patient.setDateOfBirth(LocalDate.of(1990, 5, 20));

        PatientDto dto = mapper.toDto(patient);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("Jane");
        assertThat(dto.getLastName()).isEqualTo("Doe");
        assertThat(dto.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(dto.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 20));
    }

    @Test
    void mapsDtoToEntity() {
        PatientDto dto = PatientDto.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@example.com")
                .build();

        Patient patient = mapper.toEntity(dto);

        assertThat(patient.getFirstName()).isEqualTo("John");
        assertThat(patient.getLastName()).isEqualTo("Smith");
        assertThat(patient.getEmail()).isEqualTo("john.smith@example.com");
    }
}
