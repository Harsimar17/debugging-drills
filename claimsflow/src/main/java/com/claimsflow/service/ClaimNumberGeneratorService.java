package com.claimsflow.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ClaimNumberGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(ClaimNumberGeneratorService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final Map<String, Integer> dailySequenceCounters = new ConcurrentHashMap<>();

    public String generateClaimNumber() {
        String datePart = LocalDate.now().format(DATE_FORMAT);

		int nextSequence = dailySequenceCounters.compute(datePart, (key, value) -> value == null ? 1 : value + 1);

        String claimNumber = String.format("CLM-%s-%04d", datePart, nextSequence);
        log.debug("Generated claim number {} (sequence {} for {})", claimNumber, nextSequence, datePart);
        return claimNumber;
    }

    public void resetSequenceForDate(LocalDate date) {
        dailySequenceCounters.remove(date.format(DATE_FORMAT));
    }
}
