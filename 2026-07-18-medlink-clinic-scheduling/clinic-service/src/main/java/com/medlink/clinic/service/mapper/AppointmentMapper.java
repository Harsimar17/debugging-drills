package com.medlink.clinic.service.mapper;

import com.medlink.clinic.domain.entity.Appointment;
import com.medlink.clinic.domain.entity.Patient;
import com.medlink.clinic.domain.entity.Provider;
import com.medlink.clinic.domain.entity.TimeSlot;
import com.medlink.clinic.service.dto.AppointmentResponseDto;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponseDto toDto(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        Patient patient = appointment.getPatient();
        Provider provider = appointment.getProvider();
        TimeSlot slot = appointment.getSlot();

        return AppointmentResponseDto.builder()
                .id(appointment.getId())
                .confirmationCode(appointment.getConfirmationCode())
                .patientName(patient != null ? patient.getFullName() : null)
                .providerName(provider != null ? provider.getFullName() : null)
                .date(slot != null ? slot.getSlotDate() : null)
                .startTime(slot != null ? slot.getStartTime() : null)
                .status(appointment.getStatus())
                .build();
    }
}
