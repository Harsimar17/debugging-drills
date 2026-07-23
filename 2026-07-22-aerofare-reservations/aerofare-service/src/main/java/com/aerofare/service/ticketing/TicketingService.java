package com.aerofare.service.ticketing;

import com.aerofare.common.enums.BookingStatus;
import com.aerofare.common.enums.TicketStatus;
import com.aerofare.common.exception.BookingException;
import com.aerofare.common.exception.ResourceNotFoundException;
import com.aerofare.common.util.MoneyUtil;
import com.aerofare.domain.Booking;
import com.aerofare.domain.Passenger;
import com.aerofare.domain.Ticket;
import com.aerofare.repository.BookingRepository;
import com.aerofare.repository.TicketRepository;
import com.aerofare.service.dto.TicketView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TicketingService {

    private static final Logger log = LoggerFactory.getLogger(TicketingService.class);

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;

    public TicketingService(BookingRepository bookingRepository, TicketRepository ticketRepository) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public List<TicketView> issueTickets(String recordLocator) {
        Booking booking = bookingRepository.findByRecordLocator(recordLocator)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + recordLocator));

        if (booking.getStatus() == BookingStatus.TICKETED) {
            return ticketRepository.findByBookingId(booking.getId()).stream().map(this::toView).toList();
        }
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BookingException("Booking " + recordLocator + " is " + booking.getStatus()
                    + " and cannot be ticketed");
        }

        List<Passenger> passengers = booking.getPassengers();
        int count = passengers.size();
        BigDecimal fareEach = booking.getBaseAmount().divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
        BigDecimal taxEach = booking.getTaxAmount().divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        List<TicketView> views = new ArrayList<>();
        for (Passenger passenger : passengers) {
            Ticket ticket = new Ticket(generateTicketNumber(), booking.getId(), passenger.getId(),
                    booking.getCabinClass(), MoneyUtil.normalize(fareEach), MoneyUtil.normalize(taxEach));
            Ticket saved = ticketRepository.save(ticket);
            views.add(toView(saved, passenger));
        }

        booking.setStatus(BookingStatus.TICKETED);
        bookingRepository.save(booking);
        log.info("Issued {} ticket(s) for booking {}", views.size(), recordLocator);
        return views;
    }

    private String generateTicketNumber() {
        return "TKT" + (100_000_000L + ThreadLocalRandom.current().nextLong(0, 900_000_000L));
    }

    private TicketView toView(Ticket ticket) {
        return toView(ticket, null);
    }

    private TicketView toView(Ticket ticket, Passenger passenger) {
        TicketView view = new TicketView();
        view.setTicketNumber(ticket.getTicketNumber());
        view.setPassengerId(ticket.getPassengerId());
        if (passenger != null) {
            view.setPassengerName(passenger.getFirstName() + " " + passenger.getLastName());
        }
        view.setCabinClass(ticket.getCabinClass());
        view.setFareAmount(ticket.getFareAmount());
        view.setTaxAmount(ticket.getTaxAmount());
        view.setStatus(ticket.getStatus().name());
        return view;
    }
}
