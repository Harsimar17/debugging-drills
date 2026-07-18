package com.medlink.clinic.app.config;

import com.medlink.clinic.domain.entity.Patient;
import com.medlink.clinic.domain.entity.Provider;
import com.medlink.clinic.domain.entity.TimeSlot;
import com.medlink.clinic.domain.enums.SlotStatus;
import com.medlink.clinic.repository.PatientRepository;
import com.medlink.clinic.repository.ProviderRepository;
import com.medlink.clinic.repository.TimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Seeds a small, realistic dataset on startup so the API is immediately
 * usable against the in-memory H2 database: a handful of providers, a
 * handful of patients, and a week of half-hour slots per provider.
 */
@Component
public class ClinicDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ClinicDataSeeder.class);

    private final ProviderRepository providerRepository;
    private final PatientRepository patientRepository;
    private final TimeSlotRepository timeSlotRepository;

    public ClinicDataSeeder(ProviderRepository providerRepository, PatientRepository patientRepository,
                             TimeSlotRepository timeSlotRepository) {
        this.providerRepository = providerRepository;
        this.patientRepository = patientRepository;
        this.timeSlotRepository = timeSlotRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (providerRepository.count() > 0) {
            log.info("Seed data already present, skipping.");
            return;
        }

        Provider drNair = newProvider("Priya", "Nair", "Dermatology", "1001001001");
        Provider drKim = newProvider("Alex", "Kim", "Pediatrics", "1002002002");
        Provider drOkafor = newProvider("Chidi", "Okafor", "Cardiology", "1003003003");
        providerRepository.saveAll(java.util.List.of(drNair, drKim, drOkafor));

        Patient sam = newPatient("Sam", "Rivera", "sam.rivera@example.com", "555-0101");
        Patient jane = newPatient("Jane", "Doe", "jane.doe@example.com", "555-0102");
        Patient john = newPatient("John", "Smith", "john.smith@example.com", "555-0103");
        patientRepository.saveAll(java.util.List.of(sam, jane, john));

        for (Provider provider : providerRepository.findAll()) {
            for (int dayOffset = 1; dayOffset <= 7; dayOffset++) {
                LocalDate date = LocalDate.now().plusDays(dayOffset);
                if (date.getDayOfWeek().getValue() >= 6) {
                    continue;
                }
                for (LocalTime start = LocalTime.of(9, 0); start.isBefore(LocalTime.of(12, 0)); start = start.plusMinutes(30)) {
                    TimeSlot slot = new TimeSlot();
                    slot.setProvider(provider);
                    slot.setSlotDate(date);
                    slot.setStartTime(start);
                    slot.setEndTime(start.plusMinutes(30));
                    slot.setStatus(SlotStatus.AVAILABLE);
                    timeSlotRepository.save(slot);
                }
            }
        }

        log.info("Seed data loaded: {} providers, {} patients, {} slots",
                providerRepository.count(), patientRepository.count(), timeSlotRepository.count());
    }

    private Provider newProvider(String first, String last, String specialty, String npi) {
        Provider provider = new Provider();
        provider.setFirstName(first);
        provider.setLastName(last);
        provider.setSpecialty(specialty);
        provider.setNpiNumber(npi);
        return provider;
    }

    private Patient newPatient(String first, String last, String email, String phone) {
        Patient patient = new Patient();
        patient.setFirstName(first);
        patient.setLastName(last);
        patient.setEmail(email);
        patient.setPhone(phone);
        return patient;
    }
}
