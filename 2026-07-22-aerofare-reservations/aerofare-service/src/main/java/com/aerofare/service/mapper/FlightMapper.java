package com.aerofare.service.mapper;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.domain.Flight;
import com.aerofare.service.dto.FlightView;
import com.aerofare.service.search.FlightDurationCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class FlightMapper {

    private final FlightDurationCalculator durationCalculator;

    public FlightMapper(FlightDurationCalculator durationCalculator) {
        this.durationCalculator = durationCalculator;
    }

    public FlightView toView(Flight flight, CabinClass cabinClass, int availableSeats, BigDecimal priceFrom) {
        FlightView view = new FlightView();
        view.setId(flight.getId());
        view.setFlightNumber(flight.getFlightNumber());
        view.setOriginCode(flight.getOrigin().getIataCode());
        view.setOriginCity(flight.getOrigin().getCity());
        view.setDestinationCode(flight.getDestination().getIataCode());
        view.setDestinationCity(flight.getDestination().getCity());
        view.setDepartureTime(flight.getDepartureTime());
        view.setArrivalTime(flight.getArrivalTime());
        view.setDurationMinutes(durationCalculator.durationMinutes(flight));
        view.setStatus(flight.getStatus().name());
        view.setCabinClass(cabinClass);
        view.setAvailableSeats(availableSeats);
        view.setPriceFrom(priceFrom);
        return view;
    }
}
