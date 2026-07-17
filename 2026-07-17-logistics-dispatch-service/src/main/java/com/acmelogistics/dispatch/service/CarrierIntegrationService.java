package com.acmelogistics.dispatch.service;

import com.acmelogistics.dispatch.domain.Carrier;
import com.acmelogistics.dispatch.dto.CarrierQuote;
import com.acmelogistics.dispatch.exception.CarrierIntegrationException;
import com.acmelogistics.dispatch.repository.CarrierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Talks to the (simulated) carrier rate-quote API. In production this calls
 * out over HTTPS to each carrier's rating endpoint; here we simulate realistic
 * network latency so the service behaves like it would under real conditions.
 */
@Service
public class CarrierIntegrationService {

    private static final Logger log = LoggerFactory.getLogger(CarrierIntegrationService.class);

    private final CarrierRepository carrierRepository;

    public CarrierIntegrationService(CarrierRepository carrierRepository) {
        this.carrierRepository = carrierRepository;
    }

    @Value("${dispatch.carrier.simulated-latency-min-ms:50}")
    private int minLatencyMs;

    @Value("${dispatch.carrier.simulated-latency-max-ms:250}")
    private int maxLatencyMs;

    public CarrierQuote getQuote(BigDecimal weightKg, Long preferredCarrierId) {
        List<Carrier> activeCarriers = carrierRepository.findByActiveTrue();
        if (activeCarriers.isEmpty()) {
            throw new CarrierIntegrationException("No active carriers configured");
        }

        Carrier chosen = preferredCarrierId != null
                ? activeCarriers.stream()
                    .filter(c -> c.getId().equals(preferredCarrierId))
                    .findFirst()
                    .orElse(activeCarriers.get(0))
                : activeCarriers.get(0);

        simulateNetworkLatency();

        BigDecimal baseRate = BigDecimal.valueOf(4.50);
        BigDecimal cost = baseRate.add(weightKg.multiply(BigDecimal.valueOf(1.25)))
                .setScale(2, RoundingMode.HALF_UP);

        log.info("Received quote from carrier {} for weight {}kg: {}", chosen.getCode(), weightKg, cost);

        return CarrierQuote.builder()
                .carrierId(chosen.getId())
                .carrierCode(chosen.getCode())
                .cost(cost)
                .estimatedDeliveryDays(ThreadLocalRandom.current().nextInt(2, 6))
                .build();
    }

    private void simulateNetworkLatency() {
        try {
            int delay = ThreadLocalRandom.current().nextInt(minLatencyMs, maxLatencyMs);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CarrierIntegrationException("Carrier quote request interrupted", e);
        }
    }
}
