package com.aerofare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "aircraft")
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration", nullable = false, unique = true, length = 12)
    private String registration;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "economy_seats", nullable = false)
    private int economySeats;

    @Column(name = "premium_economy_seats", nullable = false)
    private int premiumEconomySeats;

    @Column(name = "business_seats", nullable = false)
    private int businessSeats;

    @Column(name = "first_seats", nullable = false)
    private int firstSeats;

    protected Aircraft() {
    }

    public Aircraft(String registration, String model, int economySeats, int premiumEconomySeats,
                    int businessSeats, int firstSeats) {
        this.registration = registration;
        this.model = model;
        this.economySeats = economySeats;
        this.premiumEconomySeats = premiumEconomySeats;
        this.businessSeats = businessSeats;
        this.firstSeats = firstSeats;
    }

    public Long getId() {
        return id;
    }

    public String getRegistration() {
        return registration;
    }

    public String getModel() {
        return model;
    }

    public int getEconomySeats() {
        return economySeats;
    }

    public int getPremiumEconomySeats() {
        return premiumEconomySeats;
    }

    public int getBusinessSeats() {
        return businessSeats;
    }

    public int getFirstSeats() {
        return firstSeats;
    }
}
