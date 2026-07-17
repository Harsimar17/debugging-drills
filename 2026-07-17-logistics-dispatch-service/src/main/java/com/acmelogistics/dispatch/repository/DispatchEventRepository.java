package com.acmelogistics.dispatch.repository;

import com.acmelogistics.dispatch.domain.DispatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DispatchEventRepository extends JpaRepository<DispatchEvent, Long> {
    List<DispatchEvent> findByShipmentIdOrderByEventTimestampAsc(Long shipmentId);
}
