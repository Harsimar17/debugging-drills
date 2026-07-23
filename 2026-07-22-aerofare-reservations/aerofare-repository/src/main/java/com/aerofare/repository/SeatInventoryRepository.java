package com.aerofare.repository;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.domain.SeatInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {

    Optional<SeatInventory> findByFlightIdAndCabinClass(Long flightId, CabinClass cabinClass);

    List<SeatInventory> findByFlightId(Long flightId);
}
