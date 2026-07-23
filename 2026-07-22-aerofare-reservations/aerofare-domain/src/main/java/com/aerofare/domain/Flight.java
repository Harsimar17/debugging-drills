package com.aerofare.domain;

import com.aerofare.common.enums.FlightStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A scheduled flight leg. {@code departureTime} is expressed in the origin
 * airport's local time and {@code arrivalTime} in the destination airport's
 * local time.
 */
@Entity
@Table(name = "flight",
        indexes = {
                @Index(name = "idx_flight_route", columnList = "origin_code,destination_code"),
                @Index(name = "idx_flight_departure", columnList = "departure_time")
        })
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, length = 8)
    private String flightNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "origin_code", nullable = false)
    private Airport origin;

    @ManyToOne(optional = false)
    @JoinColumn(name = "destination_code", nullable = false)
    private Airport destination;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_id", nullable = false)
    private Aircraft aircraft;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FlightStatus status = FlightStatus.SCHEDULED;

    @Column(name = "base_fare", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFare;

    protected Flight() {
    }

    public Flight(String flightNumber, Airport origin, Airport destination, Aircraft aircraft,
                  LocalDateTime departureTime, LocalDateTime arrivalTime, BigDecimal baseFare) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.aircraft = aircraft;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.baseFare = baseFare;
        this.status = FlightStatus.SCHEDULED;
    }

    public Long getId() {
        return id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public Airport getOrigin() {
        return origin;
    }

    public Airport getDestination() {
        return destination;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    public BigDecimal getBaseFare() {
        return baseFare;
    }
}
