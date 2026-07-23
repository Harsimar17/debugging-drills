package com.aerofare.app.batch;

import com.aerofare.common.enums.BookingStatus;
import com.aerofare.domain.Booking;
import com.aerofare.repository.BookingRepository;
import com.aerofare.service.booking.SeatInventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Periodically expires bookings that were placed on hold but never confirmed,
 * releasing their seats back to inventory.
 */
@Component
public class BookingExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(BookingExpiryScheduler.class);

    private final BookingRepository bookingRepository;
    private final SeatInventoryService seatInventoryService;

    public BookingExpiryScheduler(BookingRepository bookingRepository,
                                  SeatInventoryService seatInventoryService) {
        this.bookingRepository = bookingRepository;
        this.seatInventoryService = seatInventoryService;
    }

    @Scheduled(fixedDelayString = "PT2M")
    @Transactional
    public void expireStaleHolds() {
        List<Booking> stale = bookingRepository.findByStatusAndHoldUntilBefore(
                BookingStatus.PENDING, LocalDateTime.now());
        if (stale.isEmpty()) {
            return;
        }
        for (Booking booking : stale) {
            booking.setStatus(BookingStatus.EXPIRED);
            seatInventoryService.release(booking.getFlightId(), booking.getCabinClass(),
                    booking.getPassengers().size());
            bookingRepository.save(booking);
        }
        log.info("Expired {} stale hold(s)", stale.size());
    }
}
