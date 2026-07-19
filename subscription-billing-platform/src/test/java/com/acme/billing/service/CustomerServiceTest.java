package com.acme.billing.service;

import com.acme.billing.domain.Customer;
import com.acme.billing.dto.CreateCustomerRequest;
import com.acme.billing.mapper.CustomerMapper;
import com.acme.billing.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        customerService = new CustomerService(customerRepository, new CustomerMapper());
    }

    @Test
    void createsCustomerWhenEmailIsUnique() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setBillingAddress("1 Main St");

        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = customerService.createCustomer(request);

        assertThat(dto.getEmail()).isEqualTo("jane@example.com");
        assertThat(dto.getFullName()).isEqualTo("Jane Doe");
    }

    @Test
    void rejectsDuplicateEmail() {
        CreateCustomerRequest request = new CreateCustomerRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setBillingAddress("1 Main St");

        when(customerRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findsExistingCustomer() {
        Customer customer = new Customer("Jane Doe", "jane@example.com", "1 Main St");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        var dto = customerService.getCustomer(1L);

        assertThat(dto.getFullName()).isEqualTo("Jane Doe");
    }
}
