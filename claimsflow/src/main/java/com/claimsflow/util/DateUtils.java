package com.claimsflow.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    private DateUtils() {
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DISPLAY_FORMAT);
    }

    public static boolean isOlderThanHours(LocalDateTime dateTime, long hours) {
        return dateTime.isBefore(LocalDateTime.now().minusHours(hours));
    }
}
