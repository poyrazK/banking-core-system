package com.cbs.customer.service;

import com.cbs.common.exception.ApiException;
import com.cbs.customer.dto.CreateCustomerRequest;
import com.cbs.customer.dto.CustomerResponse;
import com.cbs.customer.dto.UpdateKycStatusRequest;
import com.cbs.customer.model.Customer;
import com.cbs.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse createCustomer(CreateCustomerRequest request) {
        if (customerRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException("CUSTOMER_EMAIL_EXISTS", "Email is already in use");
        }

        Customer customer = new Customer(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phoneNumber(),
                request.kycStatus()
        );
        return CustomerResponse.from(customerRepository.save(customer));
    }

    public CustomerResponse getCustomer(Long customerId) {
        return CustomerResponse.from(findCustomer(customerId));
    }

    public List<CustomerResponse> searchCustomers(String query) {
        List<Customer> customers;
        if (query == null || query.isBlank()) {
            customers = customerRepository.findAll();
        } else {
            customers = customerRepository
                    .findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            query, query, query
                    );
        }
        return customers.stream().map(CustomerResponse::from).toList();
    }

    public CustomerResponse updateKycStatus(Long customerId, UpdateKycStatusRequest request) {
        Customer customer = findCustomer(customerId);
        customer.setKycStatus(request.kycStatus());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException("CUSTOMER_NOT_FOUND", "Customer not found"));
    }
}
