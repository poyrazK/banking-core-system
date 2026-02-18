package com.cbs.transaction.service;

import com.cbs.common.exception.ApiException;
import com.cbs.transaction.dto.CreateTransactionRequest;
import com.cbs.transaction.dto.ReverseTransactionRequest;
import com.cbs.transaction.dto.TransactionResponse;
import com.cbs.transaction.model.Transaction;
import com.cbs.transaction.model.TransactionStatus;
import com.cbs.transaction.model.TransactionType;
import com.cbs.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository);
    }

    @Test
    void createTransaction_normalizesReferenceCurrencyAndDescription() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                1L,
                10L,
                20L,
                TransactionType.TRANSFER,
                BigDecimal.valueOf(100.50),
                "try",
                "  monthly transfer  ",
                "  ref-001  ",
                LocalDate.of(2026, 2, 18)
        );

        when(transactionRepository.existsByReference("REF-001")).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = transactionService.createTransaction(request);

        assertEquals("REF-001", response.reference());
        assertEquals("TRY", response.currency());
        assertEquals("monthly transfer", response.description());
        assertEquals(TransactionStatus.POSTED, response.status());
    }

    @Test
    void createTransaction_throwsWhenReferenceAlreadyExists() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                1L,
                10L,
                null,
                TransactionType.PAYMENT,
                BigDecimal.TEN,
                "TRY",
                "fee",
                "REF-001",
                LocalDate.of(2026, 2, 18)
        );

        when(transactionRepository.existsByReference("REF-001")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> transactionService.createTransaction(request));

        assertEquals("TRANSACTION_REFERENCE_EXISTS", exception.getErrorCode());
        assertEquals("Reference already exists", exception.getMessage());
    }

    @Test
    void getTransaction_throwsWhenNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> transactionService.getTransaction(99L));

        assertEquals("TRANSACTION_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void reverseTransaction_throwsWhenAlreadyReversed() {
        Transaction transaction = new Transaction(
                1L,
                10L,
                null,
                TransactionType.DEPOSIT,
                BigDecimal.ONE,
                "TRY",
                "refund",
                "REF-002",
                LocalDate.of(2026, 2, 18)
        );
        transaction.setStatus(TransactionStatus.REVERSED);
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(transaction));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> transactionService.reverseTransaction(5L, new ReverseTransactionRequest("duplicate"))
        );

        assertEquals("TRANSACTION_ALREADY_REVERSED", exception.getErrorCode());
    }

    @Test
    void reverseTransaction_throwsWhenStatusIsFailed() {
        Transaction transaction = new Transaction(
                1L,
                10L,
                null,
                TransactionType.DEPOSIT,
                BigDecimal.ONE,
                "TRY",
                "refund",
                "REF-003",
                LocalDate.of(2026, 2, 18)
        );
        transaction.setStatus(TransactionStatus.FAILED);
        when(transactionRepository.findById(6L)).thenReturn(Optional.of(transaction));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> transactionService.reverseTransaction(6L, new ReverseTransactionRequest("need reversal"))
        );

        assertEquals("TRANSACTION_FAILED", exception.getErrorCode());
        assertEquals("Failed transaction cannot be reversed", exception.getMessage());
    }
}
