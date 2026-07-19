package com.acme.billing.api;

import com.acme.billing.dto.ApiResponse;
import com.acme.billing.dto.PaymentRequest;
import com.acme.billing.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ApiResponse<Void> recordPayment(@Valid @RequestBody PaymentRequest request) {
        paymentService.recordPayment(request);
        return ApiResponse.ok(null);
    }
}
