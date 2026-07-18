package com.medlink.clinic.scheduler;

import com.medlink.clinic.domain.entity.TimeSlot;
import com.medlink.clinic.domain.enums.SlotStatus;
import com.medlink.clinic.repository.TimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Housekeeping job: any slot still marked AVAILABLE after its date has
 * passed (nobody booked it and nobody cleaned it up) is cancelled so it
 * stops cluttering provider calendars.
 */
@Component
public class StaleSlotReleaseJob {

    private static final Logger log = LoggerFactory.getLogger(StaleSlotReleaseJob.class);

    private final TimeSlotRepository timeSlotRepository;

    public StaleSlotReleaseJob(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    public void cancelExpiredOpenSlots() {
        List<TimeSlot> staleSlots = timeSlotRepository
                .findBySlotDateBeforeAndStatus(LocalDate.now(), SlotStatus.AVAILABLE);

        for (TimeSlot slot : staleSlots) {
            slot.setStatus(SlotStatus.CANCELLED);
        }
        timeSlotRepository.saveAll(staleSlots);

        log.info("Stale slot cleanup: cancelled {} expired open slot(s)", staleSlots.size());
    }
}
