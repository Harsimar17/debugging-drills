package com.acme.billing.api;

import com.acme.billing.dto.ApiResponse;
import com.acme.billing.dto.CreateCustomerRequest;
import com.acme.billing.dto.CustomerDto;
import com.acme.billing.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerDto> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return ApiResponse.ok(customerService.createCustomer(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerDto> getCustomer(@PathVariable Long id) {
        return ApiResponse.ok(customerService.getCustomer(id));
    }

    @GetMapping
    public ApiResponse<List<CustomerDto>> listCustomers() {
        return ApiResponse.ok(customerService.listCustomers());
    }
}
