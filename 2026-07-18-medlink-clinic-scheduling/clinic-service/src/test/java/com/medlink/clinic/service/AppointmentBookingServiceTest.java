package com.medlink.clinic.service;

import com.medlink.clinic.common.exception.ResourceNotFoundException;
import com.medlink.clinic.common.exception.SlotUnavailableException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentBookingServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private ProviderRepository providerRepository;
    @Mock
    private TimeSlotRepository timeSlotRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AppointmentMapper appointmentMapper;

    private ProviderAvailabilityCacheService availabilityCacheService;
    private AppointmentBookingService bookingService;

    private Provider provider;
    private Patient patient;
    private TimeSlot slot;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        availabilityCacheService = new ProviderAvailabilityCacheService(timeSlotRepository);
        bookingService = new AppointmentBookingService(availabilityCacheService, patientRepository,
                providerRepository, timeSlotRepository, appointmentRepository, notificationService, appointmentMapper);

        date = LocalDate.now().plusDays(1);

        provider = new Provider();
        provider.setId(1L);
        provider.setFirstName("Alex");
        provider.setLastName("Kim");
        provider.setSpecialty("Pediatrics");
        provider.setNpiNumber("1112223334");

        patient = new Patient();
        patient.setId(10L);
        patient.setFirstName("Sam");
        patient.setLastName("Rivera");
        patient.setEmail("sam.rivera@example.com");

        slot = new TimeSlot();
        slot.setId(100L);
        slot.setProvider(provider);
        slot.setSlotDate(date);
        slot.setStartTime(LocalTime.of(14, 0));
        slot.setEndTime(LocalTime.of(14, 30));
        slot.setStatus(SlotStatus.AVAILABLE);

        lenient().when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        lenient().when(patientRepository.findById(10L)).thenReturn(Optional.of(patient));
        lenient().when(timeSlotRepository.findByProviderIdAndSlotDateAndStatusOrderByStartTimeAsc(1L, date, SlotStatus.AVAILABLE))
                .thenReturn(new ArrayList<>(List.of(slot)));
        lenient().when(timeSlotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(appointmentRepository.save(any())).thenAnswer(inv -> {
            Appointment appointment = inv.getArgument(0);
            appointment.setId(999L);
            return appointment;
        });
        lenient().when(appointmentMapper.toDto(any())).thenReturn(AppointmentResponseDto.builder().id(999L).build());
    }

    @Test
    void booksAnAvailableSlotSuccessfully() {
        AppointmentRequestDto request = new AppointmentRequestDto(10L, 1L, 100L, date);

        AppointmentResponseDto response = bookingService.book(request);

        assertThat(response.getId()).isEqualTo(999L);
        assertThat(slot.getStatus()).isEqualTo(SlotStatus.BOOKED);
        verify(timeSlotRepository).save(slot);
        verify(notificationService).sendBookingConfirmation(any());
    }

    @Test
    void rejectsBookingForUnknownSlot() {
        AppointmentRequestDto request = new AppointmentRequestDto(10L, 1L, 999L, date);

        assertThatThrownBy(() -> bookingService.book(request))
                .isInstanceOf(SlotUnavailableException.class);
    }

    @Test
    void rejectsBookingForUnknownPatient() {
        when(patientRepository.findById(55L)).thenReturn(Optional.empty());
        AppointmentRequestDto request = new AppointmentRequestDto(55L, 1L, 100L, date);

        assertThatThrownBy(() -> bookingService.book(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
