package com.aerofare.service.dto;

import com.aerofare.common.enums.PassengerType;

import java.time.LocalDate;

public class PassengerCommand {

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private PassengerType passengerType;

    public PassengerCommand() {
    }

    public PassengerCommand(String firstName, String lastName, LocalDate dateOfBirth,
                            PassengerType passengerType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.passengerType = passengerType;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public PassengerType getPassengerType() {
        return passengerType;
    }

    public void setPassengerType(PassengerType passengerType) {
        this.passengerType = passengerType;
    }
}
