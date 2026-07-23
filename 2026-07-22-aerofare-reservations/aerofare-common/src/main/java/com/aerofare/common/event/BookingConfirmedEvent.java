package com.aerofare.common.event;

import com.aerofare.common.enums.CabinClass;

import java.util.List;

/**
 * Published when a booking is confirmed. Downstream listeners react by adjusting
 * seat inventory, sending itineraries, etc. Kept as a plain immutable payload so
 * it can be shared across modules without a Spring dependency.
 */
public class BookingConfirmedEvent {

    private final Long bookingId;
    private final Long flightId;
    private final List<CabinClass> bookedCabins;

    public BookingConfirmedEvent(Long bookingId, Long flightId, List<CabinClass> bookedCabins) {
        this.bookingId = bookingId;
        this.flightId = flightId;
        this.bookedCabins = bookedCabins;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getFlightId() {
        return flightId;
    }

    public List<CabinClass> getBookedCabins() {
        return bookedCabins;
    }
}
