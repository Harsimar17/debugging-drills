package com.acme.billing.mapper;

import com.acme.billing.domain.Invoice;
import com.acme.billing.dto.InvoiceDto;
import org.springframework.stereotype.Component;

@Component
public class InvoiceMapper {

    public InvoiceDto toDto(Invoice invoice) {
        return new InvoiceDto(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getSubscription().getId(),
                invoice.getAmount(),
                invoice.getBillingPeriodStart(),
                invoice.getBillingPeriodEnd(),
                invoice.getStatus(),
                invoice.getIssuedAt()
        );
    }
}
