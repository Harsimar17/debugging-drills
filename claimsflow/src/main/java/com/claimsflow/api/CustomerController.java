package com.claimsflow.api;

import com.claimsflow.domain.Customer;
import com.claimsflow.dto.CustomerDto;
import com.claimsflow.exception.ClaimNotFoundException;
import com.claimsflow.mapper.CustomerMapper;
import com.claimsflow.repository.CustomerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerController(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @GetMapping
    public ResponseEntity<List<CustomerDto>> getAllCustomers() {
        List<CustomerDto> customers = customerRepository.findAll().stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomer(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ClaimNotFoundException("No customer found with id " + id));
        return ResponseEntity.ok(customerMapper.toDto(customer));
    }
}
