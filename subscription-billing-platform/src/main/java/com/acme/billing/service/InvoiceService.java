package com.acme.billing.service;

import com.acme.billing.domain.Invoice;
import com.acme.billing.domain.Subscription;
import com.acme.billing.dto.InvoiceDto;
import com.acme.billing.exception.ResourceNotFoundException;
import com.acme.billing.mapper.InvoiceMapper;
import com.acme.billing.repository.InvoiceRepository;
import com.acme.billing.util.MoneyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);

    private final InvoiceRepository invoiceRepository;
    private final InvoiceNumberGenerator invoiceNumberGenerator;
    private final InvoiceMapper invoiceMapper;

    public InvoiceService(InvoiceRepository invoiceRepository,
                           InvoiceNumberGenerator invoiceNumberGenerator,
                           InvoiceMapper invoiceMapper) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceNumberGenerator = invoiceNumberGenerator;
        this.invoiceMapper = invoiceMapper;
    }

    /**
     * Generates and persists an invoice for the given subscription's current billing period.
     * Runs in its own transaction so a failure invoicing one subscription during a batch
     * run does not roll back invoices already committed for other subscriptions.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Invoice generateInvoiceForRenewal(Subscription subscription) {
        LocalDate periodStart = subscription.getNextBillingDate();
        LocalDate periodEnd = subscription.getPlan().getBillingCycle().advance(periodStart);

        String invoiceNumber = invoiceNumberGenerator.next(LocalDate.now());
        Invoice invoice = new Invoice(
                invoiceNumber,
                subscription,
                MoneyUtils.round(subscription.getPlan().getPrice()),
                periodStart,
                periodEnd
        );
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Generated invoice {} for subscription {} covering {} - {}",
                saved.getInvoiceNumber(), subscription.getId(), periodStart, periodEnd);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> listBySubscription(Long subscriptionId) {
        return invoiceRepository.findBySubscriptionId(subscriptionId).stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto> listAll() {
        return invoiceRepository.findAll().stream()
                .map(invoiceMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public InvoiceDto getByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceNumber));
        return invoiceMapper.toDto(invoice);
    }
}
