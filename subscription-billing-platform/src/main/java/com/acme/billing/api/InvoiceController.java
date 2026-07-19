package com.acme.billing.api;

import com.acme.billing.dto.ApiResponse;
import com.acme.billing.dto.InvoiceDto;
import com.acme.billing.service.InvoiceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public ApiResponse<List<InvoiceDto>> listAll() {
        return ApiResponse.ok(invoiceService.listAll());
    }

    @GetMapping("/subscription/{subscriptionId}")
    public ApiResponse<List<InvoiceDto>> listBySubscription(@PathVariable Long subscriptionId) {
        return ApiResponse.ok(invoiceService.listBySubscription(subscriptionId));
    }

    @GetMapping("/{invoiceNumber}")
    public ApiResponse<InvoiceDto> getByNumber(@PathVariable String invoiceNumber) {
        return ApiResponse.ok(invoiceService.getByNumber(invoiceNumber));
    }
}
