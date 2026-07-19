package com.acme.billing.mapper;

import com.acme.billing.domain.Customer;
import com.acme.billing.dto.CustomerDto;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerDto toDto(Customer customer) {
        return new CustomerDto(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getBillingAddress(),
                customer.getCreatedAt()
        );
    }
}
