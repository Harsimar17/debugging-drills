package com.acmelogistics.dispatch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks dispatch requests that have already been processed so that retried
 * client requests (e.g. after an upstream timeout) don't trigger a second
 * carrier dispatch for the same shipment. Entries are kept in memory for a
 * bounded retention window and swept periodically, since dispatch keys are
 * only ever relevant for the lifetime of a single retry cycle.
 */
@Service
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private final Map<String, Instant> processedKeys = new HashMap<>();

    @Value("${dispatch.idempotency.retention-minutes:30}")
    private long retentionMinutes;

    /**
     * Returns true if the given idempotency key has already been marked as
     * processed and is still within its retention window.
     */
    public boolean isDuplicate(String idempotencyKey) {
        evictExpired();
        boolean duplicate = processedKeys.containsKey(idempotencyKey);
        if (duplicate) {
            log.warn("Idempotency key {} already processed, treating request as duplicate", idempotencyKey);
        }
        return duplicate;
    }

    /**
     * Marks the given idempotency key as processed so subsequent lookups
     * for the same key are recognised as duplicates.
     */
    public void markProcessed(String idempotencyKey) {
        processedKeys.put(idempotencyKey, Instant.now());
        log.debug("Marked idempotency key {} as processed ({} keys currently tracked)",
                idempotencyKey, processedKeys.size());
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(retentionMinutes, ChronoUnit.MINUTES);
        processedKeys.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
    }
}
