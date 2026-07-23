package com.aerofare.app.api;

import com.aerofare.app.dto.CreateBookingRequest;
import com.aerofare.app.dto.PassengerRequest;
import com.aerofare.service.booking.BookingService;
import com.aerofare.service.dto.BookingView;
import com.aerofare.service.dto.NewBookingCommand;
import com.aerofare.service.dto.PassengerCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingView create(@Valid @RequestBody CreateBookingRequest request) {
        NewBookingCommand command = new NewBookingCommand();
        command.setFlightNumber(request.getFlightNumber());
        command.setCabinClass(request.getCabinClass());
        command.setContactEmail(request.getContactEmail());
        command.setPassengers(request.getPassengers().stream()
                .map(this::toCommand)
                .collect(Collectors.toList()));
        return bookingService.createBooking(command);
    }

    @GetMapping("/{recordLocator}")
    public BookingView get(@PathVariable String recordLocator) {
        return bookingService.getByRecordLocator(recordLocator);
    }

    private PassengerCommand toCommand(PassengerRequest request) {
        return new PassengerCommand(request.getFirstName(), request.getLastName(),
                request.getDateOfBirth(), request.getPassengerType());
    }
}
