package com.medlink.clinic.repository;

import com.medlink.clinic.domain.entity.AppointmentNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentNotificationRepository extends JpaRepository<AppointmentNotification, Long> {

    List<AppointmentNotification> findByAppointmentId(Long appointmentId);
}
