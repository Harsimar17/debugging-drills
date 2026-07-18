package com.medlink.clinic.repository;

import com.medlink.clinic.domain.entity.TimeSlot;
import com.medlink.clinic.domain.enums.SlotStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    List<TimeSlot> findByProviderIdAndSlotDateAndStatusOrderByStartTimeAsc(
            Long providerId, LocalDate slotDate, SlotStatus status);

    List<TimeSlot> findBySlotDateBeforeAndStatus(LocalDate slotDate, SlotStatus status);
}
