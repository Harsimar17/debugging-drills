package com.medlink.clinic.repository;

import com.medlink.clinic.domain.entity.Appointment;
import com.medlink.clinic.domain.enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByConfirmationCode(String confirmationCode);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findBySlotId(Long slotId);

    List<Appointment> findByStatusAndReminderSentFalseAndSlotSlotDate(
            AppointmentStatus status, LocalDate slotDate);
}
