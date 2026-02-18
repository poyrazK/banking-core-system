package com.cbs.transaction.service;

import com.cbs.transaction.dto.CreateTransactionRequest;
import com.cbs.transaction.dto.ReverseTransactionRequest;
import com.cbs.transaction.dto.TransactionResponse;
import com.cbs.transaction.model.Transaction;
import com.cbs.transaction.model.TransactionStatus;
import com.cbs.transaction.model.TransactionType;
import com.cbs.transaction.repository.TransactionRepository;
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
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class TransactionServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55434/cbs_transaction_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @AfterEach
    void cleanUp() {
        transactionRepository.deleteAll();
    }

    @Test
    void createTransactionPersistsNormalizedReferenceInPostgres() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                101L,
                201L,
                301L,
                TransactionType.TRANSFER,
                new BigDecimal("250.00"),
                "TRY",
                "Salary transfer",
                " tx-ref-1 ",
                LocalDate.of(2026, 2, 18)
        );

        TransactionResponse response = transactionService.createTransaction(request);

        assertEquals("TX-REF-1", response.reference());
        assertTrue(transactionRepository.existsByReference("TX-REF-1"));
    }

    @Test
    void reverseTransactionPersistsStatusAndReasonInPostgres() {
        TransactionResponse created = transactionService.createTransaction(new CreateTransactionRequest(
                102L,
                202L,
                null,
                TransactionType.PAYMENT,
                new BigDecimal("80.00"),
                "USD",
                "Card payment",
                "TX-REF-2",
                LocalDate.of(2026, 2, 18)
        ));

        TransactionResponse reversed = transactionService.reverseTransaction(
                created.id(),
                new ReverseTransactionRequest(" duplicate posting ")
        );

        assertEquals(TransactionStatus.REVERSED, reversed.status());
        assertEquals("duplicate posting", reversed.reversalReason());

        Transaction persisted = transactionRepository.findById(created.id()).orElseThrow();
        assertEquals(TransactionStatus.REVERSED, persisted.getStatus());
        assertEquals("duplicate posting", persisted.getReversalReason());
    }
}