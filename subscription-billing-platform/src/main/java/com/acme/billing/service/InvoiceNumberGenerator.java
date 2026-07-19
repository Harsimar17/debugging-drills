package com.acme.billing.service;

import com.acme.billing.util.DateUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InvoiceNumberGenerator {

    private final AtomicLong sequence = new AtomicLong(0);

    public String next(LocalDate issueDate) {
        long ordinal = sequence.incrementAndGet();
        return "INV-" + DateUtils.formatForInvoiceNumber(issueDate) + "-" + String.format("%06d", ordinal);
    }
}
