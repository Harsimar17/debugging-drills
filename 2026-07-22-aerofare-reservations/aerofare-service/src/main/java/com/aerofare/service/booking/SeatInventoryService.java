package com.aerofare.service.booking;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.common.exception.SeatUnavailableException;
import com.aerofare.domain.SeatInventory;
import com.aerofare.repository.SeatInventoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeatInventoryService {

    private static final Logger log = LoggerFactory.getLogger(SeatInventoryService.class);

    private final SeatInventoryRepository seatInventoryRepository;

    public SeatInventoryService(SeatInventoryRepository seatInventoryRepository) {
        this.seatInventoryRepository = seatInventoryRepository;
    }

    public int availableSeats(Long flightId, CabinClass cabinClass) {
        return seatInventoryRepository.findByFlightIdAndCabinClass(flightId, cabinClass)
                .map(SeatInventory::getAvailableSeats)
                .orElse(0);
    }

    @Transactional
    public void reserve(Long flightId, CabinClass cabinClass, int count) {
        SeatInventory inventory = seatInventoryRepository.findByFlightIdAndCabinClass(flightId, cabinClass)
                .orElseThrow(() -> new SeatUnavailableException(
                        "No inventory for flight " + flightId + " cabin " + cabinClass));
        if (inventory.getAvailableSeats() < count) {
            throw new SeatUnavailableException("Only " + inventory.getAvailableSeats()
                    + " seat(s) left in " + cabinClass + " on flight " + flightId);
        }
        inventory.reserve(count);
        seatInventoryRepository.save(inventory);
        log.info("Reserved {} {} seat(s) on flight {}", count, cabinClass, flightId);
    }

    @Transactional
    public void release(Long flightId, CabinClass cabinClass, int count) {
        seatInventoryRepository.findByFlightIdAndCabinClass(flightId, cabinClass).ifPresent(inventory -> {
            inventory.release(count);
            seatInventoryRepository.save(inventory);
            log.info("Released {} {} seat(s) on flight {}", count, cabinClass, flightId);
        });
    }
}
