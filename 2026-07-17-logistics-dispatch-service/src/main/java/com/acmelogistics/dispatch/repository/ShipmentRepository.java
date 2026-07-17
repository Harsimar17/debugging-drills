package com.acmelogistics.dispatch.repository;

import com.acmelogistics.dispatch.domain.Shipment;
import com.acmelogistics.dispatch.domain.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    List<Shipment> findByIdempotencyKey(String idempotencyKey);

    Optional<Shipment> findFirstByIdempotencyKeyOrderByIdAsc(String idempotencyKey);

    List<Shipment> findByStatus(ShipmentStatus status);

    List<Shipment> findByOrderId(Long orderId);

    Optional<Shipment> findByTrackingNumber(String trackingNumber);
}
