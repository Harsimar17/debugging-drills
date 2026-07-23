package com.aerofare.service.mapper;

import com.aerofare.domain.Booking;
import com.aerofare.domain.Passenger;
import com.aerofare.service.dto.BookingView;
import com.aerofare.service.dto.PassengerView;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingView toView(Booking booking) {
        BookingView view = new BookingView();
        view.setId(booking.getId());
        view.setRecordLocator(booking.getRecordLocator());
        view.setContactEmail(booking.getContactEmail());
        view.setFlightId(booking.getFlightId());
        view.setCabinClass(booking.getCabinClass());
        view.setStatus(booking.getStatus());
        view.setCurrency(booking.getCurrency());
        view.setBaseAmount(booking.getBaseAmount());
        view.setTaxAmount(booking.getTaxAmount());
        view.setTotalAmount(booking.getTotalAmount());
        view.setPassengerCount(booking.getPassengers().size());
        for (Passenger passenger : booking.getPassengers()) {
            view.getPassengers().add(toPassengerView(passenger));
        }
        return view;
    }

    private PassengerView toPassengerView(Passenger passenger) {
        PassengerView pv = new PassengerView();
        pv.setId(passenger.getId());
        pv.setFirstName(passenger.getFirstName());
        pv.setLastName(passenger.getLastName());
        pv.setPassengerType(passenger.getPassengerType());
        pv.setSeatNumber(passenger.getSeatNumber());
        return pv;
    }
}
