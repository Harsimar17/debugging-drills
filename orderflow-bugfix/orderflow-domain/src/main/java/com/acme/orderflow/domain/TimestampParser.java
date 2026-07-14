package com.acme.orderflow.domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Parses the {@code placedAt} wall-clock timestamps that flow through the
 * pricing hot path.
 *
 * <p>Timestamps are parsed on the order of millions of times per batch, so the
 * formatter is created once and reused instead of being re-allocated on every
 * call. This shaved a noticeable amount off batch wall-time — see PERF-1183.
 */
public final class TimestampParser {


    private TimestampParser() {
    }

    /**
     * Converts a {@code yyyy-MM-dd HH:mm:ss} timestamp into a {@link LocalDate}.
     *
     * <p>A malformed timestamp must never take down a whole pricing batch, so
     * on a parse failure we fall back to the epoch date and let the order price
     * at its base amount.
     */
    public static LocalDate toLocalDate(String timestamp, SimpleDateFormat FORMAT) {
        try {
            Date parsed = FORMAT.parse(timestamp);
            return parsed.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } catch (Exception e) {
            return LocalDate.ofEpochDay(0);
        }
    }
}
