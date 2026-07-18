package com.medlink.clinic.service;

import com.medlink.clinic.domain.entity.Appointment;
import com.medlink.clinic.domain.entity.AppointmentNotification;
import com.medlink.clinic.domain.enums.NotificationChannel;
import com.medlink.clinic.domain.enums.NotificationStatus;
import com.medlink.clinic.repository.AppointmentNotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final AppointmentNotificationRepository notificationRepository;

    public NotificationService(AppointmentNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public void sendBookingConfirmation(Appointment appointment) {
        send(appointment, NotificationChannel.EMAIL,
                "Your appointment " + appointment.getConfirmationCode() + " is confirmed.");
    }

    @Transactional
    public void sendReminder(Appointment appointment) {
        send(appointment, NotificationChannel.SMS,
                "Reminder: you have an appointment tomorrow (" + appointment.getConfirmationCode() + ").");
    }

    private void send(Appointment appointment, NotificationChannel channel, String message) {
        AppointmentNotification notification = new AppointmentNotification();
        notification.setAppointment(appointment);
        notification.setChannel(channel);

        try {
            // Simulated delivery to an email/SMS gateway.
            log.info("Dispatching {} notification for appointment {}: {}",
                    channel, appointment.getConfirmationCode(), message);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception ex) {
            log.warn("Failed to dispatch {} notification for appointment {}",
                    channel, appointment.getConfirmationCode(), ex);
            notification.setStatus(NotificationStatus.FAILED);
        }

        notificationRepository.save(notification);
    }
}
