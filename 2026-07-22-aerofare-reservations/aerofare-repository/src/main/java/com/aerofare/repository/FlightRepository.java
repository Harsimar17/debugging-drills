package com.aerofare.repository;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.domain.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    /**
     * Flights on a route within a departure window that still have seats
     * available in the requested cabin for a party of the given size.
     */
    @Query("SELECT f FROM Flight f, SeatInventory si "
            + "WHERE si.flightId = f.id "
            + "AND f.origin.iataCode = :origin "
            + "AND f.destination.iataCode = :destination "
            + "AND f.departureTime >= :start "
            + "AND f.departureTime < :end "
            + "AND si.cabinClass = :cabin "
            + "AND (si.totalSeats - si.bookedSeats) > :passengers "
            + "ORDER BY f.departureTime")
    List<Flight> searchAvailable(@Param("origin") String origin,
                                 @Param("destination") String destination,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end,
                                 @Param("cabin") CabinClass cabin,
                                 @Param("passengers") int passengers);

    List<Flight> findByStatus(com.aerofare.common.enums.FlightStatus status);
}
