package com.cbs.payment.service;

import com.cbs.payment.dto.CreatePaymentRequest;
import com.cbs.payment.dto.PaymentResponse;
import com.cbs.payment.dto.PaymentStatusUpdateRequest;
import com.cbs.payment.model.Payment;
import com.cbs.payment.model.PaymentMethod;
import com.cbs.payment.model.PaymentStatus;
import com.cbs.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "ledger.posting.enabled=false"
})
class PaymentServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55435/cbs_payment_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void cleanUp() {
        paymentRepository.deleteAll();
    }

    @Test
    void createPaymentPersistsNormalizedReferenceInPostgres() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                101L,
                501L,
                601L,
                new BigDecimal("300.00"),
                "TRY",
                PaymentMethod.BANK_TRANSFER,
                " pay-001 ",
                "Invoice payment",
                LocalDate.of(2026, 2, 18)
        );

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("PAY-001", response.reference());
        assertTrue(paymentRepository.existsByReference("PAY-001"));
    }

    @Test
    void failPaymentPersistsStatusAndReasonInPostgres() {
        PaymentResponse created = paymentService.createPayment(new CreatePaymentRequest(
                102L,
                502L,
                602L,
                new BigDecimal("120.00"),
                "USD",
                PaymentMethod.SWIFT,
                "PAY-002",
                "International transfer",
                LocalDate.of(2026, 2, 18)
        ));

        PaymentResponse failed = paymentService.failPayment(
                created.id(),
                new PaymentStatusUpdateRequest(" network timeout ")
        );

        assertEquals(PaymentStatus.FAILED, failed.status());
        assertEquals("network timeout", failed.failureReason());

        Payment persisted = paymentRepository.findById(created.id()).orElseThrow();
        assertEquals(PaymentStatus.FAILED, persisted.getStatus());
        assertEquals("network timeout", persisted.getFailureReason());
    }
}