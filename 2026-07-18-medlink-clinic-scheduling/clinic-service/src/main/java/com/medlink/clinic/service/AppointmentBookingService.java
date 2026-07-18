package com.medlink.clinic.service;

import com.medlink.clinic.common.exception.ResourceNotFoundException;
import com.medlink.clinic.common.exception.SlotUnavailableException;
import com.medlink.clinic.common.util.ConfirmationCodeGenerator;
import com.medlink.clinic.domain.entity.Appointment;
import com.medlink.clinic.domain.entity.Patient;
import com.medlink.clinic.domain.entity.Provider;
import com.medlink.clinic.domain.entity.TimeSlot;
import com.medlink.clinic.domain.enums.AppointmentStatus;
import com.medlink.clinic.domain.enums.SlotStatus;
import com.medlink.clinic.repository.AppointmentRepository;
import com.medlink.clinic.repository.PatientRepository;
import com.medlink.clinic.repository.ProviderRepository;
import com.medlink.clinic.repository.TimeSlotRepository;
import com.medlink.clinic.service.dto.AppointmentRequestDto;
import com.medlink.clinic.service.dto.AppointmentResponseDto;
import com.medlink.clinic.service.mapper.AppointmentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentBookingService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentBookingService.class);

    private final ProviderAvailabilityCacheService availabilityCacheService;
    private final PatientRepository patientRepository;
    private final ProviderRepository providerRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    private final AppointmentMapper appointmentMapper;

    public AppointmentBookingService(ProviderAvailabilityCacheService availabilityCacheService,
                                      PatientRepository patientRepository,
                                      ProviderRepository providerRepository,
                                      TimeSlotRepository timeSlotRepository,
                                      AppointmentRepository appointmentRepository,
                                      NotificationService notificationService,
                                      AppointmentMapper appointmentMapper) {
        this.availabilityCacheService = availabilityCacheService;
        this.patientRepository = patientRepository;
        this.providerRepository = providerRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.appointmentRepository = appointmentRepository;
        this.notificationService = notificationService;
        this.appointmentMapper = appointmentMapper;
    }

    @Transactional
    public AppointmentResponseDto book(AppointmentRequestDto request) {
        log.info("Booking request received: patient={} provider={} slot={} date={}",
                request.getPatientId(), request.getProviderId(), request.getSlotId(), request.getDate());

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + request.getPatientId()));
        Provider provider = providerRepository.findById(request.getProviderId())
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + request.getProviderId()));

        List<TimeSlot> availableSlots = availabilityCacheService.getAvailableSlots(
                request.getProviderId(), request.getDate());

        TimeSlot chosenSlot = availableSlots.stream()
                .filter(slot -> slot.getId().equals(request.getSlotId()))
                .findFirst()
                .orElseThrow(() -> new SlotUnavailableException(
                        "Slot " + request.getSlotId() + " is no longer available"));

        int updated = timeSlotRepository.tryReserve(request.getSlotId());
        
        if (updated == 0) 
        {
            throw new SlotUnavailableException("Slot " + request.getSlotId() + " is no longer available");
        }
        
        chosenSlot.setStatus(SlotStatus.BOOKED);
        timeSlotRepository.save(chosenSlot);

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setProvider(provider);
        appointment.setSlot(chosenSlot);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        appointment.setConfirmationCode(ConfirmationCodeGenerator.generate());
        Appointment saved = appointmentRepository.save(appointment);

        log.info("Appointment confirmed: id={} code={} patient={} provider={} slot={}",
                saved.getId(), saved.getConfirmationCode(), patient.getId(), provider.getId(), chosenSlot.getId());

        notificationService.sendBookingConfirmation(saved);

        return appointmentMapper.toDto(saved);
    }

    @Transactional
    public void cancel(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found: " + appointmentId));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        TimeSlot slot = appointment.getSlot();
        slot.setStatus(SlotStatus.AVAILABLE);
        timeSlotRepository.save(slot);
        appointmentRepository.save(appointment);

        availabilityCacheService.evict(appointment.getProvider().getId(), slot.getSlotDate());
        log.info("Appointment cancelled: id={} slot={} released back to provider={}",
                appointmentId, slot.getId(), appointment.getProvider().getId());
    }
}
