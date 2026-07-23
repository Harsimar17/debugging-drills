package com.aerofare.domain;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.common.enums.TicketStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket",
        indexes = @Index(name = "idx_ticket_booking", columnList = "booking_id"))
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 20)
    private String ticketNumber;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cabin_class", nullable = false, length = 20)
    private CabinClass cabinClass;

    @Column(name = "fare_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal fareAmount;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status = TicketStatus.ISSUED;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    protected Ticket() {
    }

    public Ticket(String ticketNumber, Long bookingId, Long passengerId, CabinClass cabinClass,
                  BigDecimal fareAmount, BigDecimal taxAmount) {
        this.ticketNumber = ticketNumber;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.cabinClass = cabinClass;
        this.fareAmount = fareAmount;
        this.taxAmount = taxAmount;
        this.status = TicketStatus.ISSUED;
        this.issuedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getPassengerId() {
        return passengerId;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public BigDecimal getFareAmount() {
        return fareAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
}
