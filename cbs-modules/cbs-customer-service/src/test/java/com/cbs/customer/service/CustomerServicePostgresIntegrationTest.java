package com.cbs.customer.service;

import com.cbs.customer.dto.CreateCustomerRequest;
import com.cbs.customer.dto.CustomerResponse;
import com.cbs.customer.dto.UpdateKycStatusRequest;
import com.cbs.customer.model.Customer;
import com.cbs.customer.model.KycStatus;
import com.cbs.customer.repository.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CustomerServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55445/cbs_customer_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @AfterEach
    void cleanUp() {
        customerRepository.deleteAll();
    }

    @Test
    void createCustomerPersistsDefaultKycStatusInPostgres() {
        CustomerResponse response = customerService.createCustomer(new CreateCustomerRequest(
                "Jane",
                "Doe",
                "jane@cbs.com",
                "+90-555-0101",
                null
        ));

        assertEquals(KycStatus.PENDING, response.kycStatus());
        assertTrue(customerRepository.existsByEmailIgnoreCase("jane@cbs.com"));
    }

    @Test
    void updateKycStatusPersistsChangeInPostgres() {
        CustomerResponse created = customerService.createCustomer(new CreateCustomerRequest(
                "Ali",
                "Can",
                "ali@cbs.com",
                "+90-555-0102",
                KycStatus.PENDING
        ));

        CustomerResponse updated = customerService.updateKycStatus(created.id(), new UpdateKycStatusRequest(KycStatus.VERIFIED));
        assertEquals(KycStatus.VERIFIED, updated.kycStatus());

        Customer persisted = customerRepository.findById(created.id()).orElseThrow();
        assertEquals(KycStatus.VERIFIED, persisted.getKycStatus());
    }
}