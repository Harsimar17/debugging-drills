package com.aerofare.service.search;

import com.aerofare.domain.Flight;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Computes the elapsed flying time of a flight leg from its scheduled departure
 * and arrival times. Used both for display on search results and for the
 * long-haul surcharge applied during pricing.
 */
@Component
public class FlightDurationCalculator {

    /**
     * Block time of the leg, in minutes.
     */
    public long durationMinutes(Flight flight) {
        Duration duration = Duration.between(flight.getDepartureTime(), flight.getArrivalTime());
        return duration.toMinutes();
    }

    public boolean isLongHaul(Flight flight) {
        return durationMinutes(flight) >= 360;
    }
}
