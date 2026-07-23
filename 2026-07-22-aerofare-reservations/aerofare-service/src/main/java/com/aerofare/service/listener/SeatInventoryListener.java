package com.aerofare.service.listener;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.common.event.BookingConfirmedEvent;
import com.aerofare.service.booking.SeatInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reacts to confirmed bookings by decrementing seat inventory for the booked
 * cabins. Runs within the booking transaction so a shortfall rolls the booking
 * back.
 */
@Component
public class SeatInventoryListener {

    private static final Logger log = LoggerFactory.getLogger(SeatInventoryListener.class);

    private final SeatInventoryService seatInventoryService;

    public SeatInventoryListener(SeatInventoryService seatInventoryService) {
        this.seatInventoryService = seatInventoryService;
    }

    @EventListener
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        Map<CabinClass, Long> countsByCabin = event.getBookedCabins().stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        log.info("Applying seat inventory for booking {} on flight {}: {}",
                event.getBookingId(), event.getFlightId(), countsByCabin);

        countsByCabin.forEach((cabin, count) ->
                seatInventoryService.reserve(event.getFlightId(), cabin, count.intValue()));
    }
}
