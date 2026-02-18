package com.cbs.payment.service;

import com.cbs.common.exception.ApiException;
import com.cbs.payment.dto.CreatePaymentRequest;
import com.cbs.payment.dto.PaymentResponse;
import com.cbs.payment.dto.PaymentStatusUpdateRequest;
import com.cbs.payment.integration.LedgerPostingClient;
import com.cbs.payment.model.Payment;
import com.cbs.payment.model.PaymentMethod;
import com.cbs.payment.model.PaymentStatus;
import com.cbs.payment.repository.PaymentRepository;
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
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private LedgerPostingClient ledgerPostingClient;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository, ledgerPostingClient);
    }

    @Test
    void createPayment_normalizesReferenceCurrencyAndDescription() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                1L,
                10L,
                20L,
                BigDecimal.valueOf(150.25),
                "try",
                PaymentMethod.BANK_TRANSFER,
                "  pay-001  ",
                "  utility bill  ",
                LocalDate.of(2026, 2, 18)
        );

        when(paymentRepository.existsByReference("PAY-001")).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(ledgerPostingClient).postPayment(any(Payment.class));

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals("PAY-001", response.reference());
        assertEquals("TRY", response.currency());
        assertEquals("utility bill", response.description());
        assertEquals(PaymentStatus.COMPLETED, response.status());
    }

    @Test
    void createPayment_throwsWhenReferenceAlreadyExists() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                1L,
                10L,
                null,
                BigDecimal.TEN,
                "TRY",
                PaymentMethod.CARD,
                "PAY-001",
                "card payment",
                LocalDate.of(2026, 2, 18)
        );
        when(paymentRepository.existsByReference("PAY-001")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> paymentService.createPayment(request));

        assertEquals("PAYMENT_REFERENCE_EXISTS", exception.getErrorCode());
    }

    @Test
    void createPayment_marksFailedWhenLedgerPostingFails() {
        CreatePaymentRequest request = new CreatePaymentRequest(
                1L,
                10L,
                20L,
                BigDecimal.valueOf(75.00),
                "TRY",
                PaymentMethod.CARD,
                "PAY-010",
                "Utility",
                LocalDate.of(2026, 2, 18)
        );

        when(paymentRepository.existsByReference("PAY-010")).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new ApiException("LEDGER_POSTING_FAILED", "ledger unavailable"))
                .when(ledgerPostingClient)
                .postPayment(any(Payment.class));

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals(PaymentStatus.FAILED, response.status());
        assertEquals("ledger unavailable", response.failureReason());
    }

    @Test
    void completePayment_throwsWhenCurrentStatusIsCompleted() {
        Payment payment = createPaymentWithStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(11L)).thenReturn(Optional.of(payment));

        ApiException exception = assertThrows(ApiException.class, () -> paymentService.completePayment(11L));

        assertEquals("PAYMENT_ALREADY_COMPLETED", exception.getErrorCode());
        assertEquals("Completed payment cannot be changed", exception.getMessage());
    }

    @Test
    void failPayment_setsFailureStatusAndReason() {
        Payment payment = createPaymentWithStatus(PaymentStatus.INITIATED);
        when(paymentRepository.findById(12L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.failPayment(12L, new PaymentStatusUpdateRequest("  insufficient funds  "));

        assertEquals(PaymentStatus.FAILED, response.status());
        assertEquals("insufficient funds", response.failureReason());
    }

    @Test
    void completePayment_clearsFailureReason() {
        Payment payment = createPaymentWithStatus(PaymentStatus.INITIATED);
        payment.setFailureReason("temporary issue");
        when(paymentRepository.findById(13L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.completePayment(13L);

        assertEquals(PaymentStatus.COMPLETED, response.status());
        assertNull(response.failureReason());
    }

    @Test
    void retryPosting_completesFailedPaymentWhenLedgerSucceeds() {
        Payment payment = createPaymentWithStatus(PaymentStatus.FAILED);
        payment.setFailureReason("timeout");
        when(paymentRepository.findById(21L)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(ledgerPostingClient).postPayment(any(Payment.class));

        PaymentResponse response = paymentService.retryPosting(21L);

        assertEquals(PaymentStatus.COMPLETED, response.status());
        assertNull(response.failureReason());
    }

    @Test
    void retryPosting_throwsWhenPaymentIsNotFailed() {
        Payment payment = createPaymentWithStatus(PaymentStatus.COMPLETED);
        when(paymentRepository.findById(22L)).thenReturn(Optional.of(payment));

        ApiException exception = assertThrows(ApiException.class, () -> paymentService.retryPosting(22L));

        assertEquals("PAYMENT_NOT_FAILED", exception.getErrorCode());
    }

    private Payment createPaymentWithStatus(PaymentStatus status) {
        Payment payment = new Payment(
                1L,
                10L,
                20L,
                BigDecimal.valueOf(100.00),
                "TRY",
                PaymentMethod.BANK_TRANSFER,
                "PAY-002",
                "payment",
                LocalDate.of(2026, 2, 18)
        );
        payment.setStatus(status);
        return payment;
    }
}
