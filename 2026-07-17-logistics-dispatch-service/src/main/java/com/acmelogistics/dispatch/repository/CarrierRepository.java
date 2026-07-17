package com.acmelogistics.dispatch.repository;

import com.acmelogistics.dispatch.domain.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarrierRepository extends JpaRepository<Carrier, Long> {
    List<Carrier> findByActiveTrue();
    Optional<Carrier> findByCode(String code);
}
