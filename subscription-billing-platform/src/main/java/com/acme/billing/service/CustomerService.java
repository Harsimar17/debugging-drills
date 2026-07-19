package com.acme.billing.service;

import com.acme.billing.domain.Customer;
import com.acme.billing.dto.CreateCustomerRequest;
import com.acme.billing.dto.CustomerDto;
import com.acme.billing.exception.ResourceNotFoundException;
import com.acme.billing.mapper.CustomerMapper;
import com.acme.billing.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional
    public CustomerDto createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("A customer with this email already exists");
        }
        Customer customer = new Customer(request.getFullName(), request.getEmail(), request.getBillingAddress());
        Customer saved = customerRepository.save(customer);
        log.info("Created customer {} ({})", saved.getId(), saved.getEmail());
        return customerMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        return customerMapper.toDto(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerDto> listCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toDto)
                .toList();
    }
}
