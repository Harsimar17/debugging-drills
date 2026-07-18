package com.claimsflow.config;

import com.claimsflow.domain.Adjuster;
import com.claimsflow.domain.Customer;
import com.claimsflow.domain.Policy;
import com.claimsflow.domain.enums.PolicyType;
import com.claimsflow.repository.AdjusterRepository;
import com.claimsflow.repository.CustomerRepository;
import com.claimsflow.repository.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataSeedConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSeedConfig.class);

    @Bean
    public CommandLineRunner seedData(CustomerRepository customerRepository,
                                       PolicyRepository policyRepository,
                                       AdjusterRepository adjusterRepository) {
        return args -> {
            if (customerRepository.count() > 0) {
                log.info("Seed data already present, skipping seeding.");
                return;
            }

            Customer alice = new Customer();
            alice.setFirstName("Alice");
            alice.setLastName("Nguyen");
            alice.setEmail("alice.nguyen@example.com");
            alice.setPhoneNumber("555-0101");
            alice.setDateOfBirth(LocalDate.of(1988, 4, 12));
            customerRepository.save(alice);

            Customer bob = new Customer();
            bob.setFirstName("Bob");
            bob.setLastName("Martinez");
            bob.setEmail("bob.martinez@example.com");
            bob.setPhoneNumber("555-0102");
            bob.setDateOfBirth(LocalDate.of(1975, 11, 3));
            customerRepository.save(bob);

            Policy alicePolicy = new Policy();
            alicePolicy.setPolicyNumber("POL-AUTO-1001");
            alicePolicy.setPolicyType(PolicyType.AUTO);
            alicePolicy.setCoverageAmount(new BigDecimal("25000.00"));
            alicePolicy.setPremiumAmount(new BigDecimal("1200.00"));
            alicePolicy.setStartDate(LocalDate.now().minusMonths(6));
            alicePolicy.setEndDate(LocalDate.now().plusMonths(6));
            alicePolicy.setCustomer(alice);
            policyRepository.save(alicePolicy);

            Policy bobPolicy = new Policy();
            bobPolicy.setPolicyNumber("POL-HOME-2001");
            bobPolicy.setPolicyType(PolicyType.HOME);
            bobPolicy.setCoverageAmount(new BigDecimal("350000.00"));
            bobPolicy.setPremiumAmount(new BigDecimal("2100.00"));
            bobPolicy.setStartDate(LocalDate.now().minusMonths(3));
            bobPolicy.setEndDate(LocalDate.now().plusMonths(9));
            bobPolicy.setCustomer(bob);
            policyRepository.save(bobPolicy);

            Adjuster carol = new Adjuster();
            carol.setFullName("Carol Jennings");
            carol.setEmployeeCode("ADJ-001");
            carol.setRegion("NORTHEAST");
            adjusterRepository.save(carol);

            Adjuster dan = new Adjuster();
            dan.setFullName("Dan Okoye");
            dan.setEmployeeCode("ADJ-002");
            dan.setRegion("NORTHEAST");
            adjusterRepository.save(dan);

            log.info("Seed data loaded: {} customers, {} policies, {} adjusters",
                    customerRepository.count(), policyRepository.count(), adjusterRepository.count());
        };
    }
}
