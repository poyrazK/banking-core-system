package com.cbs.customer.service;

import com.cbs.common.exception.ApiException;
import com.cbs.customer.dto.CreateCustomerRequest;
import com.cbs.customer.dto.CustomerResponse;
import com.cbs.customer.dto.UpdateKycStatusRequest;
import com.cbs.customer.model.Customer;
import com.cbs.customer.model.KycStatus;
import com.cbs.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(customerRepository);
    }

    @Test
    void createCustomer_defaultsKycToPendingWhenMissing() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Jane",
                "Doe",
                "jane@cbs.com",
                "+90-555-0101",
                null
        );
        when(customerRepository.existsByEmailIgnoreCase("jane@cbs.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.createCustomer(request);

        assertEquals("Jane", response.firstName());
        assertEquals("jane@cbs.com", response.email());
        assertEquals(KycStatus.PENDING, response.kycStatus());
    }

    @Test
    void createCustomer_throwsWhenEmailAlreadyExists() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Jane",
                "Doe",
                "jane@cbs.com",
                "+90-555-0101",
                KycStatus.VERIFIED
        );
        when(customerRepository.existsByEmailIgnoreCase("jane@cbs.com")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> customerService.createCustomer(request));

        assertEquals("CUSTOMER_EMAIL_EXISTS", exception.getErrorCode());
        assertEquals("Email is already in use", exception.getMessage());
    }

    @Test
    void searchCustomers_returnsAllWhenQueryIsBlank() {
        when(customerRepository.findAll()).thenReturn(List.of(
                new Customer("Jane", "Doe", "jane@cbs.com", "+90-555-0101", KycStatus.PENDING)
        ));

        List<CustomerResponse> result = customerService.searchCustomers("  ");

        assertEquals(1, result.size());
        assertEquals("jane@cbs.com", result.get(0).email());
    }

    @Test
    void updateKycStatus_updatesAndReturnsSavedCustomer() {
        Customer customer = new Customer("Jane", "Doe", "jane@cbs.com", "+90-555-0101", KycStatus.PENDING);
        when(customerRepository.findById(11L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse result = customerService.updateKycStatus(11L, new UpdateKycStatusRequest(KycStatus.VERIFIED));

        assertEquals(KycStatus.VERIFIED, result.kycStatus());
    }
}