package com.aerofare.app.api;

import com.aerofare.service.dto.TicketView;
import com.aerofare.service.ticketing.TicketingService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings/{recordLocator}/tickets")
public class TicketController {

    private final TicketingService ticketingService;

    public TicketController(TicketingService ticketingService) {
        this.ticketingService = ticketingService;
    }

    @PostMapping
    public List<TicketView> issue(@PathVariable String recordLocator) {
        return ticketingService.issueTickets(recordLocator);
    }
}
