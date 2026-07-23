package com.aerofare.app.api;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.service.booking.SeatInventoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final SeatInventoryService seatInventoryService;

    public AdminController(SeatInventoryService seatInventoryService) {
        this.seatInventoryService = seatInventoryService;
    }

    @GetMapping("/inventory")
    public Map<String, Object> inventory(@RequestParam Long flightId,
                                         @RequestParam CabinClass cabin) {
        int available = seatInventoryService.availableSeats(flightId, cabin);
        return Map.of("flightId", flightId, "cabin", cabin, "availableSeats", available);
    }
}
