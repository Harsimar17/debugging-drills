package com.aerofare.domain;

import com.aerofare.common.enums.BookingStatus;
import com.aerofare.common.enums.CabinClass;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "booking",
        indexes = @Index(name = "idx_booking_pnr", columnList = "record_locator"))
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "record_locator", nullable = false, unique = true, length = 6)
    private String recordLocator;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cabin_class", nullable = false, length = 20)
    private CabinClass cabinClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Column(name = "base_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "hold_until")
    private LocalDateTime holdUntil;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Passenger> passengers = new ArrayList<>();

    @Version
    @Column(name = "version")
    private Long version;

    protected Booking() {
    }

    public Booking(String recordLocator, String contactEmail, Long flightId, CabinClass cabinClass) {
        this.recordLocator = recordLocator;
        this.contactEmail = contactEmail;
        this.flightId = flightId;
        this.cabinClass = cabinClass;
        this.status = BookingStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void addPassenger(Passenger passenger) {
        passenger.setBooking(this);
        this.passengers.add(passenger);
    }

    public Long getId() {
        return id;
    }

    public String getRecordLocator() {
        return recordLocator;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public Long getFlightId() {
        return flightId;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getHoldUntil() {
        return holdUntil;
    }

    public void setHoldUntil(LocalDateTime holdUntil) {
        this.holdUntil = holdUntil;
    }

    public List<Passenger> getPassengers() {
        return passengers;
    }
}
