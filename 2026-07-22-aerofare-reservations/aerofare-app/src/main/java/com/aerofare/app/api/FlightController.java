package com.aerofare.app.api;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.service.dto.FlightView;
import com.aerofare.service.search.FlightSearchService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightSearchService flightSearchService;

    public FlightController(FlightSearchService flightSearchService) {
        this.flightSearchService = flightSearchService;
    }

    @GetMapping("/search")
    public List<FlightView> search(@RequestParam String origin,
                                   @RequestParam String destination,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                   @RequestParam(defaultValue = "ECONOMY") CabinClass cabin,
                                   @RequestParam(defaultValue = "1") int passengers) {
        return flightSearchService.search(origin, destination, date, cabin, passengers);
    }
}
