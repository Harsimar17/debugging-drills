package com.medlink.clinic.scheduler;

import com.medlink.clinic.domain.entity.Appointment;
import com.medlink.clinic.domain.enums.AppointmentStatus;
import com.medlink.clinic.repository.AppointmentRepository;
import com.medlink.clinic.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Sends a reminder notification for every confirmed appointment scheduled
 * for tomorrow that hasn't already had a reminder sent.
 */
@Component
public class AppointmentReminderJob {

    private static final Logger log = LoggerFactory.getLogger(AppointmentReminderJob.class);

    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;

    public AppointmentReminderJob(AppointmentRepository appointmentRepository, NotificationService notificationService) {
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 18 * * *")
    @Transactional
    public void sendTomorrowReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> dueForReminder = appointmentRepository
                .findByStatusAndReminderSentFalseAndSlotSlotDate(AppointmentStatus.CONFIRMED, tomorrow);

        log.info("Reminder job started: {} appointment(s) due for {}", dueForReminder.size(), tomorrow);

        for (Appointment appointment : dueForReminder) {
            try {
                notificationService.sendReminder(appointment);
                appointment.setReminderSent(true);
                appointmentRepository.save(appointment);
            } catch (Exception ex) {
                log.error("Failed to send reminder for appointment {}", appointment.getId(), ex);
            }
        }

        log.info("Reminder job finished for {}", tomorrow);
    }
}
