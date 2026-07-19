package com.acme.billing.api;

import com.acme.billing.dto.ApiResponse;
import com.acme.billing.dto.CreateSubscriptionRequest;
import com.acme.billing.dto.SubscriptionDto;
import com.acme.billing.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubscriptionDto> createSubscription(@Valid @RequestBody CreateSubscriptionRequest request) {
        return ApiResponse.ok(subscriptionService.createSubscription(request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<SubscriptionDto> cancelSubscription(@PathVariable Long id) {
        return ApiResponse.ok(subscriptionService.cancelSubscription(id));
    }

    @GetMapping("/{id}")
    public ApiResponse<SubscriptionDto> getSubscription(@PathVariable Long id) {
        return ApiResponse.ok(subscriptionService.getSubscription(id));
    }

    @GetMapping
    public ApiResponse<List<SubscriptionDto>> listByCustomer(@RequestParam Long customerId) {
        return ApiResponse.ok(subscriptionService.listByCustomer(customerId));
    }
}
