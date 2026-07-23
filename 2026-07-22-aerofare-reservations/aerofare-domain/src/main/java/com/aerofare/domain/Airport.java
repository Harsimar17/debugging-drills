package com.aerofare.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * An airport. {@code zoneId} is the IANA time-zone identifier (e.g.
 * "America/New_York") in which this airport's local clock runs — flight
 * departure and arrival times are expressed in their respective airport's
 * local time.
 */
@Entity
@Table(name = "airport")
public class Airport {

    @Id
    @Column(name = "iata_code", length = 3)
    private String iataCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "zone_id", nullable = false)
    private String zoneId;

    protected Airport() {
    }

    public Airport(String iataCode, String name, String city, String country, String zoneId) {
        this.iataCode = iataCode;
        this.name = name;
        this.city = city;
        this.country = country;
        this.zoneId = zoneId;
    }

    public String getIataCode() {
        return iataCode;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public String getZoneId() {
        return zoneId;
    }
}
