package com.aerofare.service.pricing;

import com.aerofare.common.enums.CabinClass;
import com.aerofare.common.enums.PassengerType;
import com.aerofare.common.util.MoneyUtil;
import com.aerofare.domain.FareRule;
import com.aerofare.domain.Flight;
import com.aerofare.service.dto.FareQuote;
import com.aerofare.service.search.FlightDurationCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Prices a party on a flight: sums per-passenger fares (base fare × fare-rule
 * multiplier), adds a long-haul surcharge, tax and a per-booking service fee.
 */
@Service
public class FareCalculationService {

    private static final Logger log = LoggerFactory.getLogger(FareCalculationService.class);

    private static final String CURRENCY = "USD";
    private static final BigDecimal TAX_RATE_PERCENT = new BigDecimal("12.00");
    private static final BigDecimal LONG_HAUL_SURCHARGE = new BigDecimal("45.00");
    private static final BigDecimal BOOKING_SERVICE_FEE = new BigDecimal("20.00");

    private final PricingRuleEngine pricingRuleEngine;
    private final FlightDurationCalculator durationCalculator;
    private final FareCache fareCache;

    public FareCalculationService(PricingRuleEngine pricingRuleEngine,
                                  FlightDurationCalculator durationCalculator,
                                  FareCache fareCache) {
        this.pricingRuleEngine = pricingRuleEngine;
        this.durationCalculator = durationCalculator;
        this.fareCache = fareCache;
    }

    public FareQuote quote(Flight flight, CabinClass cabin, List<PassengerType> passengers) {
        FareQuote cached = fareCache.get(flight.getId() + cabin.toString() + passengers);
        if (cached != null) {
            log.debug("Fare cache hit for flight {}", flight.getId());
            return cached;
        }

        BigDecimal base = BigDecimal.ZERO;
        for (PassengerType passengerType : passengers) {
            FareRule rule = pricingRuleEngine.resolve(cabin, passengerType);
            BigDecimal passengerFare = flight.getBaseFare().multiply(rule.getFareMultiplier());
            base = base.add(passengerFare);
        }

        if (durationCalculator.isLongHaul(flight)) {
            base = base.add(LONG_HAUL_SURCHARGE.multiply(BigDecimal.valueOf(passengers.size())));
        }
        base = MoneyUtil.normalize(base);

        BigDecimal tax = MoneyUtil.percentage(base, TAX_RATE_PERCENT);

        // The flat booking service fee is shared evenly across the party.
        BigDecimal perPassengerFee = BOOKING_SERVICE_FEE.divide(BigDecimal.valueOf(passengers.size()),  RoundingMode.HALF_UP);
        BigDecimal fee = MoneyUtil.normalize(perPassengerFee.multiply(BigDecimal.valueOf(passengers.size())));

        BigDecimal total = MoneyUtil.normalize(base.add(tax).add(fee));

        FareQuote quote = new FareQuote(CURRENCY, base, tax, fee, total);
        fareCache.put(flight.getId() + cabin.toString() + passengers, quote);
        log.info("Priced flight {} cabin {} for {} passenger(s): total {}",
                flight.getId(), cabin, passengers.size(), total);
        return quote;
    }
}
