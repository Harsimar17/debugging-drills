package com.aerofare.service.dto;

import com.aerofare.common.enums.CabinClass;

import java.util.ArrayList;
import java.util.List;

public class NewBookingCommand {

    private String flightNumber;
    private CabinClass cabinClass;
    private String contactEmail;
    private List<PassengerCommand> passengers = new ArrayList<>();

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public List<PassengerCommand> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerCommand> passengers) {
        this.passengers = passengers;
    }
}
