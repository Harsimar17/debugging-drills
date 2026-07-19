package com.acme.billing.service;

import com.acme.billing.domain.Invoice;
import com.acme.billing.domain.InvoiceStatus;
import com.acme.billing.domain.Payment;
import com.acme.billing.dto.PaymentRequest;
import com.acme.billing.exception.ResourceNotFoundException;
import com.acme.billing.repository.InvoiceRepository;
import com.acme.billing.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public void recordPayment(PaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + request.getInvoiceId()));

        Payment payment = new Payment(invoice, invoice.getAmount(), request.getPaymentReference());
        paymentRepository.save(payment);
        invoice.setStatus(InvoiceStatus.PAID);
        log.info("Recorded payment {} for invoice {}", request.getPaymentReference(), invoice.getInvoiceNumber());
    }
}
