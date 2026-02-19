package com.cbs.customer.service;

import com.cbs.common.exception.ApiException;
import com.cbs.customer.dto.CreateCustomerRequest;
import com.cbs.customer.dto.CustomerResponse;
import com.cbs.customer.dto.UpdateCustomerRequest;
import com.cbs.customer.dto.UpdateKycStatusRequest;
import com.cbs.customer.model.Customer;
import com.cbs.customer.model.KycStatus;
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
                request.firstName().trim(),
                request.lastName().trim(),
                request.email().trim(),
                request.phoneNumber().trim(),
                request.kycStatus());
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
                            query, query, query);
        }
        return customers.stream().map(CustomerResponse::from).toList();
    }

    public CustomerResponse updateKycStatus(Long customerId, UpdateKycStatusRequest request) {
        Customer customer = findCustomer(customerId);
        customer.setKycStatus(request.kycStatus());
        return CustomerResponse.from(customerRepository.save(customer));
    }

    public CustomerResponse updateCustomer(Long customerId, UpdateCustomerRequest request) {
        Customer customer = findCustomer(customerId);
        boolean identityChanged = false;

        if (request.email() != null && !request.email().equalsIgnoreCase(customer.getEmail())) {
            String newEmail = request.email().trim();
            if (customerRepository.existsByEmailIgnoreCaseAndIdNot(newEmail, customerId)) {
                throw new ApiException("CUSTOMER_EMAIL_EXISTS", "Email is already in use by another customer");
            }
            customer.setEmail(newEmail);
        }

        if (request.firstName() != null) {
            String newFirstName = request.firstName().trim();
            if (!newFirstName.equals(customer.getFirstName())) {
                customer.setFirstName(newFirstName);
                identityChanged = true;
            }
        }
        if (request.lastName() != null) {
            String newLastName = request.lastName().trim();
            if (!newLastName.equals(customer.getLastName())) {
                customer.setLastName(newLastName);
                identityChanged = true;
            }
        }
        if (request.phoneNumber() != null) {
            customer.setPhoneNumber(request.phoneNumber().trim());
        }

        if (identityChanged && customer.getKycStatus() == KycStatus.VERIFIED) {
            customer.setKycStatus(KycStatus.PENDING);
        }

        return CustomerResponse.from(customerRepository.save(customer));
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ApiException("CUSTOMER_NOT_FOUND", "Customer not found"));
    }
}
