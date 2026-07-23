package com.aerofare.service.search;

import com.aerofare.domain.Flight;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
    	ZonedDateTime dep = flight.getDepartureTime()
                .atZone(ZoneId.of(flight.getOrigin().getZoneId()));
        ZonedDateTime arr = flight.getArrivalTime()
                .atZone(ZoneId.of(flight.getDestination().getZoneId()));
        return Duration.between(dep, arr).toMinutes();
    }

    public boolean isLongHaul(Flight flight) {
        return durationMinutes(flight) >= 360;
    }
}
