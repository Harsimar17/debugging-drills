package com.aerofare.app.batch;

import com.aerofare.common.enums.FlightStatus;
import com.aerofare.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Heartbeat job that reports how many flights are currently in the schedule.
 * A hook for future automated status transitions (boarding, departed, ...).
 */
@Component
public class FlightStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(FlightStatusScheduler.class);

    private final FlightRepository flightRepository;

    public FlightStatusScheduler(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Scheduled(fixedDelayString = "PT5M")
    public void reportScheduledFlights() {
        long scheduled = flightRepository.findByStatus(FlightStatus.SCHEDULED).size();
        log.debug("Flight status heartbeat: {} scheduled flight(s)", scheduled);
    }
}
