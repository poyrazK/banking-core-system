package com.cbs.transaction.service;

import com.cbs.common.exception.ApiException;
import com.cbs.transaction.dto.CreateTransactionRequest;
import com.cbs.transaction.dto.ReverseTransactionRequest;
import com.cbs.transaction.dto.TransactionResponse;
import com.cbs.transaction.integration.AccountClient;
import com.cbs.transaction.integration.LedgerPostingClient;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LedgerPostingClient ledgerPostingClient;

    @Mock
    private AccountClient accountClient;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, ledgerPostingClient, accountClient);
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
                LocalDate.of(2026, 2, 18));

        when(transactionRepository.existsByReference("REF-001")).thenReturn(false);
        when(accountClient.getAccountCurrency(10L)).thenReturn("TRY");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(ledgerPostingClient).postTransaction(any(Transaction.class));

        TransactionResponse response = transactionService.createTransaction(request);

        assertEquals("REF-001", response.reference());
        assertEquals("TRY", response.currency());
        assertEquals("monthly transfer", response.description());
        assertEquals(TransactionStatus.POSTED, response.status());
    }

    @Test
    void createTransaction_marksFailedWhenLedgerPostingFails() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                1L,
                10L,
                20L,
                TransactionType.PAYMENT,
                BigDecimal.valueOf(40.50),
                "TRY",
                "bill pay",
                "REF-FAIL",
                LocalDate.of(2026, 2, 18));

        when(transactionRepository.existsByReference("REF-FAIL")).thenReturn(false);
        when(accountClient.getAccountCurrency(10L)).thenReturn("TRY");
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new ApiException("LEDGER_POSTING_FAILED", "ledger timeout"))
                .when(ledgerPostingClient)
                .postTransaction(any(Transaction.class));

        TransactionResponse response = transactionService.createTransaction(request);

        assertEquals(TransactionStatus.FAILED, response.status());
        assertEquals("ledger timeout", response.failureReason());
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
                LocalDate.of(2026, 2, 18));

        when(transactionRepository.existsByReference("REF-001")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> transactionService.createTransaction(request));

        assertEquals("TRANSACTION_REFERENCE_EXISTS", exception.getErrorCode());
        assertEquals("Reference already exists", exception.getMessage());
    }

    @Test
    void createTransaction_throwsWhenCurrencyMismatch() {
        CreateTransactionRequest request = new CreateTransactionRequest(
                1L,
                10L,
                null,
                TransactionType.DEPOSIT,
                BigDecimal.valueOf(100),
                "USD",
                "deposit",
                "REF-USD",
                LocalDate.of(2026, 2, 18));

        when(transactionRepository.existsByReference("REF-USD")).thenReturn(false);
        when(accountClient.getAccountCurrency(10L)).thenReturn("TRY");

        ApiException exception = assertThrows(ApiException.class, () -> transactionService.createTransaction(request));

        assertEquals("CURRENCY_MISMATCH", exception.getErrorCode());
        assertEquals("Account currency is TRY but transaction uses USD", exception.getMessage());
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
                LocalDate.of(2026, 2, 18));
        transaction.setStatus(TransactionStatus.REVERSED);
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(transaction));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> transactionService.reverseTransaction(5L, new ReverseTransactionRequest("duplicate")));

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
                LocalDate.of(2026, 2, 18));
        transaction.setStatus(TransactionStatus.FAILED);
        when(transactionRepository.findById(6L)).thenReturn(Optional.of(transaction));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> transactionService.reverseTransaction(6L, new ReverseTransactionRequest("need reversal")));

        assertEquals("TRANSACTION_FAILED", exception.getErrorCode());
        assertEquals("Failed transaction cannot be reversed", exception.getMessage());
    }

    @Test
    void retryPosting_postsFailedTransactionWhenLedgerSucceeds() {
        Transaction transaction = new Transaction(
                1L,
                10L,
                20L,
                TransactionType.TRANSFER,
                BigDecimal.valueOf(55.00),
                "TRY",
                "retry",
                "REF-500",
                LocalDate.of(2026, 2, 18));
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setFailureReason("timeout");

        when(transactionRepository.findById(50L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(ledgerPostingClient).postTransaction(any(Transaction.class));

        TransactionResponse response = transactionService.retryPosting(50L);

        assertEquals(TransactionStatus.POSTED, response.status());
        assertNull(response.failureReason());
    }

    @Test
    void retryPosting_throwsWhenTransactionIsNotFailed() {
        Transaction transaction = new Transaction(
                1L,
                10L,
                null,
                TransactionType.DEPOSIT,
                BigDecimal.ONE,
                "TRY",
                "x",
                "REF-501",
                LocalDate.of(2026, 2, 18));
        transaction.setStatus(TransactionStatus.POSTED);

        when(transactionRepository.findById(51L)).thenReturn(Optional.of(transaction));

        ApiException exception = assertThrows(ApiException.class, () -> transactionService.retryPosting(51L));

        assertEquals("TRANSACTION_NOT_FAILED", exception.getErrorCode());
    }
}
