package com.acmelogistics.dispatch.config;

import com.acmelogistics.dispatch.domain.Carrier;
import com.acmelogistics.dispatch.repository.CarrierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CarrierRepository carrierRepository;

    public DataSeeder(CarrierRepository carrierRepository) {
        this.carrierRepository = carrierRepository;
    }

    @Override
    public void run(String... args) {
        if (carrierRepository.count() > 0) {
            log.info("Carrier data already present, skipping seed");
            return;
        }

        carrierRepository.save(Carrier.builder()
                .name("SwiftHaul Freight")
                .code("SWFT")
                .contactEmail("ops@swifthaul.example.com")
                .active(true)
                .build());

        carrierRepository.save(Carrier.builder()
                .name("Meridian Express")
                .code("MRDN")
                .contactEmail("dispatch@meridianexpress.example.com")
                .active(true)
                .build());

        carrierRepository.save(Carrier.builder()
                .name("Coastal Logistics Co.")
                .code("CSTL")
                .contactEmail("support@coastallogistics.example.com")
                .active(false)
                .build());

        log.info("Seeded default carrier data");
    }
}
