package com.aerofare.service.pricing;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.common.enums.PassengerType;
import com.aerofare.common.exception.FareException;
import com.aerofare.domain.FareRule;
import com.aerofare.repository.FareRuleRepository;
import org.springframework.stereotype.Component;

@Component
public class PricingRuleEngine {

    private final FareRuleRepository fareRuleRepository;

    public PricingRuleEngine(FareRuleRepository fareRuleRepository) {
        this.fareRuleRepository = fareRuleRepository;
    }

    /**
     * Resolve the fare rule for a (cabin, passenger-type). Falls back to the
     * cabin's ADULT rule when a specific passenger-type rule is not configured.
     */
    public FareRule resolve(CabinClass cabin, PassengerType passengerType) {
        return fareRuleRepository.findByCabinClassAndPassengerType(cabin, passengerType)
                .or(() -> fareRuleRepository.findByCabinClassAndPassengerType(cabin, PassengerType.ADULT))
                .orElseThrow(() -> new FareException(
                        "No fare rule configured for cabin " + cabin + " / " + passengerType));
    }
}
