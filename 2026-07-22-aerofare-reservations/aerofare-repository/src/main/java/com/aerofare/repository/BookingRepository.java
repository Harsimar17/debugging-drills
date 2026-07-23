package com.aerofare.repository;

import com.aerofare.common.enums.BookingStatus;
import com.aerofare.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByRecordLocator(String recordLocator);

    boolean existsByRecordLocator(String recordLocator);

    List<Booking> findByStatusAndHoldUntilBefore(BookingStatus status, LocalDateTime cutoff);
}
