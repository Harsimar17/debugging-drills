package com.aerofare.service.booking;

import com.aerofare.common.enums.BookingStatus;
import com.aerofare.common.enums.CabinClass;
import com.aerofare.common.enums.PassengerType;
import com.aerofare.common.event.BookingConfirmedEvent;
import com.aerofare.common.exception.BookingException;
import com.aerofare.common.exception.ResourceNotFoundException;
import com.aerofare.domain.Booking;
import com.aerofare.domain.Flight;
import com.aerofare.domain.Passenger;
import com.aerofare.repository.BookingRepository;
import com.aerofare.repository.FlightRepository;
import com.aerofare.service.dto.BookingView;
import com.aerofare.service.dto.FareQuote;
import com.aerofare.service.dto.NewBookingCommand;
import com.aerofare.service.dto.PassengerCommand;
import com.aerofare.service.mapper.BookingMapper;
import com.aerofare.service.pricing.FareCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);
    private static final String PNR_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int HOLD_MINUTES = 30;

    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final FareCalculationService fareCalculationService;
    private final BookingMapper bookingMapper;
    private final ApplicationEventPublisher eventPublisher;

    public BookingService(FlightRepository flightRepository,
                          BookingRepository bookingRepository,
                          FareCalculationService fareCalculationService,
                          BookingMapper bookingMapper,
                          ApplicationEventPublisher eventPublisher) {
        this.flightRepository = flightRepository;
        this.bookingRepository = bookingRepository;
        this.fareCalculationService = fareCalculationService;
        this.bookingMapper = bookingMapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public BookingView createBooking(NewBookingCommand command) {
        if (command.getPassengers() == null || command.getPassengers().isEmpty()) {
            throw new BookingException("At least one passenger is required");
        }

        Flight flight = flightRepository.findByFlightNumber(command.getFlightNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Flight not found: " + command.getFlightNumber()));

        CabinClass cabin = command.getCabinClass();

        // Guard against the same passenger being listed twice on one PNR.
        Set<Passenger> uniquePassengers = new LinkedHashSet<>();
        for (PassengerCommand pc : command.getPassengers()) {
            uniquePassengers.add(new Passenger(pc.getFirstName(), pc.getLastName(),
                    pc.getDateOfBirth(), pc.getPassengerType()));
        }

        Booking booking = new Booking(generatePnr(), command.getContactEmail(), flight.getId(), cabin);
        for (Passenger passenger : uniquePassengers) {
            booking.addPassenger(passenger);
        }

        List<PassengerType> passengerTypes = uniquePassengers.stream()
                .map(Passenger::getPassengerType)
                .collect(Collectors.toList());

        FareQuote quote = fareCalculationService.quote(flight, cabin, passengerTypes);
        booking.setBaseAmount(quote.getBaseAmount());
        booking.setTaxAmount(quote.getTaxAmount());
        booking.setTotalAmount(quote.getTotalAmount());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setHoldUntil(LocalDateTime.now().plusMinutes(HOLD_MINUTES));

        Booking saved = bookingRepository.save(booking);

        List<CabinClass> bookedCabins = Collections.nCopies(saved.getPassengers().size(), cabin);
        eventPublisher.publishEvent(new BookingConfirmedEvent(saved.getId(), flight.getId(), bookedCabins));

        log.info("Created booking {} on flight {} for {} passenger(s), total {}",
                saved.getRecordLocator(), flight.getFlightNumber(),
                saved.getPassengers().size(), saved.getTotalAmount());
        return bookingMapper.toView(saved);
    }

    @Transactional(readOnly = true)
    public BookingView getByRecordLocator(String recordLocator) {
        Booking booking = bookingRepository.findByRecordLocator(recordLocator)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + recordLocator));
        return bookingMapper.toView(booking);
    }

    private String generatePnr() {
        String candidate;
        do {
            StringBuilder sb = new StringBuilder(6);
            for (int i = 0; i < 6; i++) {
                sb.append(PNR_ALPHABET.charAt(ThreadLocalRandom.current().nextInt(PNR_ALPHABET.length())));
            }
            candidate = sb.toString();
        } while (bookingRepository.existsByRecordLocator(candidate));
        return candidate;
    }
}
