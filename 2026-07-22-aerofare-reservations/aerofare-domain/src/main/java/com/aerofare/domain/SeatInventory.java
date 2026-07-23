package com.aerofare.domain;

import com.aerofare.common.enums.CabinClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Per-cabin seat inventory for a flight. {@code bookedSeats} rises as bookings
 * are confirmed; availability is {@code totalSeats - bookedSeats}.
 */
@Entity
@Table(name = "seat_inventory",
        indexes = @Index(name = "idx_inventory_flight", columnList = "flight_id"))
public class SeatInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cabin_class", nullable = false, length = 20)
    private CabinClass cabinClass;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "booked_seats", nullable = false)
    private int bookedSeats;

    @Version
    @Column(name = "version")
    private Long version;

    protected SeatInventory() {
    }

    public SeatInventory(Long flightId, CabinClass cabinClass, int totalSeats) {
        this.flightId = flightId;
        this.cabinClass = cabinClass;
        this.totalSeats = totalSeats;
        this.bookedSeats = 0;
    }

    public Long getId() {
        return id;
    }

    public Long getFlightId() {
        return flightId;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public int getAvailableSeats() {
        return totalSeats - bookedSeats;
    }

    public void reserve(int count) {
        this.bookedSeats += count;
    }

    public void release(int count) {
        this.bookedSeats = Math.max(0, this.bookedSeats - count);
    }
}
