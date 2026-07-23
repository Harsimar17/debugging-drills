package com.aerofare.service.search;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.common.enums.PassengerType;
import com.aerofare.domain.Flight;
import com.aerofare.repository.FlightRepository;
import com.aerofare.service.booking.SeatInventoryService;
import com.aerofare.service.dto.FlightView;
import com.aerofare.service.mapper.FlightMapper;
import com.aerofare.service.pricing.FareCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FlightSearchService {

    private static final Logger log = LoggerFactory.getLogger(FlightSearchService.class);

    private final FlightRepository flightRepository;
    private final SeatInventoryService seatInventoryService;
    private final FareCalculationService fareCalculationService;
    private final FlightMapper flightMapper;

    public FlightSearchService(FlightRepository flightRepository,
                               SeatInventoryService seatInventoryService,
                               FareCalculationService fareCalculationService,
                               FlightMapper flightMapper) {
        this.flightRepository = flightRepository;
        this.seatInventoryService = seatInventoryService;
        this.fareCalculationService = fareCalculationService;
        this.flightMapper = flightMapper;
    }

    @Transactional(readOnly = true)
    public List<FlightView> search(String origin, String destination, LocalDate date,
                                   CabinClass cabin, int passengers) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Flight> flights = flightRepository.searchAvailable(origin, destination, start, end, cabin, passengers);
        log.info("Search {}->{} on {} cabin {} pax {} matched {} flight(s)",
                origin, destination, date, cabin, passengers, flights.size());

        List<FlightView> results = new ArrayList<>();
        for (Flight flight : flights) {
            int available = seatInventoryService.availableSeats(flight.getId(), cabin);
            BigDecimal priceFrom = fareCalculationService
                    .quote(flight, cabin, List.of(PassengerType.ADULT))
                    .getTotalAmount();
            results.add(flightMapper.toView(flight, cabin, available, priceFrom));
        }
        return results;
    }
}
