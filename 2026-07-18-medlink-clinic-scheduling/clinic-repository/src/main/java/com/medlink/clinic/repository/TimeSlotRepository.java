package com.medlink.clinic.repository;

import com.medlink.clinic.domain.entity.TimeSlot;
import com.medlink.clinic.domain.enums.SlotStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

	@Modifying
	@Query("UPDATE TimeSlot t SET t.status = com.medlink.clinic.domain.enums.SlotStatus.BOOKED " +
	       "WHERE t.id = :slotId AND t.status = com.medlink.clinic.domain.enums.SlotStatus.AVAILABLE")
	int tryReserve(@Param("slotId") Long slotId);
    List<TimeSlot> findByProviderIdAndSlotDateAndStatusOrderByStartTimeAsc(
            Long providerId, LocalDate slotDate, SlotStatus status);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TimeSlot> findByProviderIdAndSlotDateAndStatusAndIdOrderByStartTimeAsc(
            Long providerId, LocalDate slotDate, SlotStatus status, Long slotId);

    List<TimeSlot> findBySlotDateBeforeAndStatus(LocalDate slotDate, SlotStatus status);
}
