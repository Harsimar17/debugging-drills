package com.aerofare.repository;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.common.enums.PassengerType;
import com.aerofare.domain.FareRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FareRuleRepository extends JpaRepository<FareRule, Long> {

    Optional<FareRule> findByCabinClassAndPassengerType(CabinClass cabinClass, PassengerType passengerType);
}
