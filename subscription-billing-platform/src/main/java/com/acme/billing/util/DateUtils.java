package com.acme.billing.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    private static final DateTimeFormatter INVOICE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private DateUtils() {
    }

    public static String formatForInvoiceNumber(LocalDate date) {
        return date.format(INVOICE_DATE_FORMAT);
    }
}
