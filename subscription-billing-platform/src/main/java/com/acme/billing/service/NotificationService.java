package com.acme.billing.service;

import com.acme.billing.domain.Customer;
import com.acme.billing.domain.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void sendInvoiceNotification(Customer customer, Invoice invoice) {
        // In production this publishes to the email delivery queue. For now we log it.
        log.info("Sending invoice {} ({}) to {}", invoice.getInvoiceNumber(), invoice.getAmount(), customer.getEmail());
    }
}
