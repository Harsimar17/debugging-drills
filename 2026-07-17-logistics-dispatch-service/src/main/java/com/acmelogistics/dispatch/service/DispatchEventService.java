package com.acmelogistics.dispatch.service;

import com.acmelogistics.dispatch.domain.DispatchEvent;
import com.acmelogistics.dispatch.domain.DispatchEventType;
import com.acmelogistics.dispatch.repository.DispatchEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DispatchEventService {

    private static final Logger log = LoggerFactory.getLogger(DispatchEventService.class);

    private final DispatchEventRepository dispatchEventRepository;

    public DispatchEventService(DispatchEventRepository dispatchEventRepository) {
        this.dispatchEventRepository = dispatchEventRepository;
    }

    @Transactional
    public void record(Long shipmentId, DispatchEventType type, String details) {
        DispatchEvent event = DispatchEvent.builder()
                .shipmentId(shipmentId)
                .eventType(type)
                .details(details)
                .build();
        dispatchEventRepository.save(event);
        log.debug("Recorded event {} for shipment {}: {}", type, shipmentId, details);
    }
}
