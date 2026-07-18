package com.medlink.clinic.service;

import com.medlink.clinic.domain.entity.TimeSlot;
import com.medlink.clinic.domain.enums.SlotStatus;
import com.medlink.clinic.repository.TimeSlotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Provider availability changes constantly but the "get open slots for
 * provider X on day Y" query is by far the hottest read path in the booking
 * flow (every calendar page view and every booking attempt calls it), and it
 * hits the same handful of (provider, date) keys thousands of times an hour
 * during business hours. We cache the result so a busy provider's day isn't
 * re-queried from Postgres on every request.
 */
@Service
public class ProviderAvailabilityCacheService {

    private static final Logger log = LoggerFactory.getLogger(ProviderAvailabilityCacheService.class);

    private final TimeSlotRepository timeSlotRepository;

    public ProviderAvailabilityCacheService(TimeSlotRepository timeSlotRepository) {
        this.timeSlotRepository = timeSlotRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "providerAvailability", key = "#providerId + '-' + #date")
    public List<TimeSlot> getAvailableSlots(Long providerId, LocalDate date) {
        log.debug("Cache miss - loading available slots for provider={} date={}", providerId, date);
        return timeSlotRepository.findByProviderIdAndSlotDateAndStatusOrderByStartTimeAsc(
                providerId, date, SlotStatus.AVAILABLE);
    }

    @CacheEvict(value = "providerAvailability", key = "#providerId + '-' + #date")
    public void evict(Long providerId, LocalDate date) {
        log.debug("Evicting availability cache for provider={} date={}", providerId, date);
    }
}
