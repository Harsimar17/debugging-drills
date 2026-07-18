package com.medlink.clinic.service;

import com.medlink.clinic.domain.entity.Appointment;
import com.medlink.clinic.domain.entity.Patient;
import com.medlink.clinic.domain.entity.Provider;
import com.medlink.clinic.domain.entity.TimeSlot;
import com.medlink.clinic.domain.enums.SlotStatus;
import com.medlink.clinic.repository.AppointmentRepository;
import com.medlink.clinic.repository.PatientRepository;
import com.medlink.clinic.repository.ProviderRepository;
import com.medlink.clinic.repository.TimeSlotRepository;
import com.medlink.clinic.service.dto.AppointmentRequestDto;
import com.medlink.clinic.service.dto.AppointmentResponseDto;
import com.medlink.clinic.service.mapper.AppointmentMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Reproduces the reported production symptom: a handful of patients calling
 * the booking API for the exact same open slot at the exact same moment
 * (e.g. two browser tabs, a mobile app double-tap, or a retried request from
 * a flaky network) should result in exactly one confirmed appointment and
 * everyone else getting "slot no longer available".
 *
 * This test fires a burst of concurrent requests for a single slot and
 * asserts that at most one of them wins.
 */
class ConcurrentBookingSimulationTest {

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        ConcurrentMapCacheManager cacheManager() {
            return new ConcurrentMapCacheManager("providerAvailability");
        }

        @Bean
        TimeSlotRepository timeSlotRepository() {
            return mock(TimeSlotRepository.class);
        }

        @Bean
        PatientRepository patientRepository() {
            return mock(PatientRepository.class);
        }

        @Bean
        ProviderRepository providerRepository() {
            return mock(ProviderRepository.class);
        }

        @Bean
        AppointmentRepository appointmentRepository() {
            return mock(AppointmentRepository.class);
        }

        @Bean
        NotificationService notificationService() {
            return mock(NotificationService.class);
        }

        @Bean
        AppointmentMapper appointmentMapper() {
            return mock(AppointmentMapper.class);
        }

        @Bean
        ProviderAvailabilityCacheService availabilityCacheService(TimeSlotRepository repo) {
            return new ProviderAvailabilityCacheService(repo);
        }

        @Bean
        AppointmentBookingService bookingService(ProviderAvailabilityCacheService cacheService,
                                                  PatientRepository patientRepository,
                                                  ProviderRepository providerRepository,
                                                  TimeSlotRepository timeSlotRepository,
                                                  AppointmentRepository appointmentRepository,
                                                  NotificationService notificationService,
                                                  AppointmentMapper appointmentMapper) {
            return new AppointmentBookingService(cacheService, patientRepository, providerRepository,
                    timeSlotRepository, appointmentRepository, notificationService, appointmentMapper);
        }
    }

    private AnnotationConfigApplicationContext context;

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void onlyOneConcurrentRequestShouldWinTheSameSlot() throws InterruptedException {
        context = new AnnotationConfigApplicationContext(TestConfig.class);

        TimeSlotRepository timeSlotRepository = context.getBean(TimeSlotRepository.class);
        PatientRepository patientRepository = context.getBean(PatientRepository.class);
        ProviderRepository providerRepository = context.getBean(ProviderRepository.class);
        AppointmentRepository appointmentRepository = context.getBean(AppointmentRepository.class);
        AppointmentMapper appointmentMapper = context.getBean(AppointmentMapper.class);
        AppointmentBookingService bookingService = context.getBean(AppointmentBookingService.class);

        Long providerId = 42L;
        LocalDate date = LocalDate.now().plusDays(1);

        Provider provider = new Provider();
        provider.setId(providerId);
        provider.setFirstName("Priya");
        provider.setLastName("Nair");
        provider.setSpecialty("Dermatology");
        provider.setNpiNumber("9988776655");

        TimeSlot slot = new TimeSlot();
        slot.setId(500L);
        slot.setProvider(provider);
        slot.setSlotDate(date);
        slot.setStartTime(LocalTime.of(10, 0));
        slot.setEndTime(LocalTime.of(10, 30));
        slot.setStatus(SlotStatus.AVAILABLE);

        when(timeSlotRepository.findByProviderIdAndSlotDateAndStatusOrderByStartTimeAsc(
                eq(providerId), eq(date), eq(SlotStatus.AVAILABLE)))
                .thenReturn(new ArrayList<>(List.of(slot)));
        when(timeSlotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(providerRepository.findById(providerId)).thenReturn(Optional.of(provider));
        when(patientRepository.findById(anyLong())).thenAnswer(inv -> {
            Long id = inv.getArgument(0);
            Patient patient = new Patient();
            patient.setId(id);
            patient.setFirstName("Patient");
            patient.setLastName(String.valueOf(id));
            patient.setEmail("patient" + id + "@example.com");
            return Optional.of(patient);
        });

        AtomicLong appointmentIdGenerator = new AtomicLong(1);
        List<Appointment> savedAppointments = java.util.Collections.synchronizedList(new ArrayList<>());
        when(appointmentRepository.save(any())).thenAnswer(inv -> {
            Appointment appointment = inv.getArgument(0);
            appointment.setId(appointmentIdGenerator.getAndIncrement());
            savedAppointments.add(appointment);
            return appointment;
        });
        when(appointmentMapper.toDto(any())).thenReturn(AppointmentResponseDto.builder().build());

        int concurrentPatients = 12;
        ExecutorService pool = Executors.newFixedThreadPool(concurrentPatients);
        CountDownLatch readyLatch = new CountDownLatch(concurrentPatients);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger rejectedCount = new AtomicInteger();

        for (int i = 0; i < concurrentPatients; i++) {
            long patientId = 1000L + i;
            pool.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    AppointmentRequestDto request = new AppointmentRequestDto(patientId, providerId, slot.getId(), date);
                    bookingService.book(request);
                    successCount.incrementAndGet();
                } catch (Exception ex) {
                    rejectedCount.incrementAndGet();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        long appointmentsForThisSlot = savedAppointments.stream()
                .filter(a -> a.getSlot().getId().equals(slot.getId()))
                .count();

        assertThat(successCount.get() + rejectedCount.get()).isEqualTo(concurrentPatients);
        assertThat(appointmentsForThisSlot)
                .as("exactly one appointment should ever be confirmed for a single slot")
                .isEqualTo(1);
    }
}
