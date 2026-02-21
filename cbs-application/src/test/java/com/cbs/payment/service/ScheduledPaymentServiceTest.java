package com.cbs.payment.service;

import com.cbs.common.exception.ApiException;
import com.cbs.payment.dto.CreateScheduledPaymentRequest;
import com.cbs.payment.dto.ScheduledPaymentResponse;
import com.cbs.payment.model.PaymentMethod;
import com.cbs.payment.model.ScheduleFrequency;
import com.cbs.payment.model.ScheduledPayment;
import com.cbs.payment.model.ScheduledPaymentStatus;
import com.cbs.payment.repository.ScheduledPaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledPaymentServiceTest {

    @Mock
    private ScheduledPaymentRepository scheduledPaymentRepository;

    @Mock
    private PaymentService paymentService;

    private ScheduledPaymentService scheduledPaymentService;

    @BeforeEach
    void setUp() {
        scheduledPaymentService = new ScheduledPaymentService(scheduledPaymentRepository, paymentService);
    }

    @Test
    void createScheduledPayment_savesCorrectly() {
        CreateScheduledPaymentRequest request = new CreateScheduledPaymentRequest(
                1L, 101L, 201L, new BigDecimal("100.00"), "TRY",
                PaymentMethod.BANK_TRANSFER, "SCH-REF-1", "Rent",
                ScheduleFrequency.MONTHLY, LocalDate.now(), null);

        when(scheduledPaymentRepository.existsByReference("SCH-REF-1")).thenReturn(false);
        when(scheduledPaymentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ScheduledPaymentResponse response = scheduledPaymentService.createScheduledPayment(request);

        assertNotNull(response);
        assertEquals("SCH-REF-1", response.reference());
        assertEquals(ScheduledPaymentStatus.ACTIVE, response.status());
        assertEquals(LocalDate.now(), response.nextExecutionDate());
    }

    @Test
    void executeScheduledPayments_processesDuePayments() {
        ScheduledPayment payment = new ScheduledPayment(
                1L, 101L, 201L, new BigDecimal("100.00"), "TRY",
                PaymentMethod.BANK_TRANSFER, "Rent", ScheduleFrequency.MONTHLY,
                LocalDate.now().minusDays(1), null, "SCH-REF-1");
        // nextExecutionDate is startDate by default in constructor, but let's be
        // explicit
        payment.setNextExecutionDate(LocalDate.now().minusDays(1));

        when(scheduledPaymentRepository.findByNextExecutionDateLessThanEqualAndStatus(any(),
                eq(ScheduledPaymentStatus.ACTIVE)))
                .thenReturn(List.of(payment));

        scheduledPaymentService.executeScheduledPayments();

        verify(paymentService, times(1)).createPayment(any());
        verify(scheduledPaymentRepository, times(1)).save(payment);

        // Assert next date is +1 month from due date (yesterday)
        assertEquals(LocalDate.now().minusDays(1).plusMonths(1), payment.getNextExecutionDate());
        assertEquals(1, payment.getExecutionCount());
        assertEquals(LocalDate.now(), payment.getLastExecutedDate());
    }

    @Test
    void executeScheduledPayments_completesAfterEndDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        ScheduledPayment payment = new ScheduledPayment(
                1L, 101L, 201L, new BigDecimal("100.00"), "TRY",
                PaymentMethod.BANK_TRANSFER, "Limited Pay", ScheduleFrequency.DAILY,
                yesterday, yesterday, "SCH-REF-2");
        payment.setNextExecutionDate(yesterday);

        when(scheduledPaymentRepository.findByNextExecutionDateLessThanEqualAndStatus(any(),
                eq(ScheduledPaymentStatus.ACTIVE)))
                .thenReturn(List.of(payment));

        scheduledPaymentService.executeScheduledPayments();

        assertEquals(ScheduledPaymentStatus.COMPLETED, payment.getStatus());
        assertNull(payment.getNextExecutionDate());
    }

    @Test
    void executeScheduledPayments_autoPausesAfterFailures() {
        ScheduledPayment payment = new ScheduledPayment(
                1L, 101L, 201L, new BigDecimal("100.00"), "TRY",
                PaymentMethod.BANK_TRANSFER, "Fail Test", ScheduleFrequency.DAILY,
                LocalDate.now(), null, "SCH-REF-FAIL");
        payment.setFailureCount(2);

        when(scheduledPaymentRepository.findByNextExecutionDateLessThanEqualAndStatus(any(),
                eq(ScheduledPaymentStatus.ACTIVE)))
                .thenReturn(List.of(payment));
        doThrow(new ApiException("insufficient balance", "Insufficient balance")).when(paymentService)
                .createPayment(any());

        scheduledPaymentService.executeScheduledPayments();

        assertEquals(3, payment.getFailureCount());
        assertEquals(ScheduledPaymentStatus.PAUSED, payment.getStatus());
        assertTrue(payment.getLastFailureReason().toLowerCase().contains("insufficient balance"));
    }

    @Test
    void pauseAndResume_updatesStatus() {
        ScheduledPayment payment = new ScheduledPayment(
                1L, 101L, 201L, new BigDecimal("100.00"), "TRY",
                PaymentMethod.BANK_TRANSFER, "Status Test", ScheduleFrequency.MONTHLY,
                LocalDate.now().plusDays(1), null, "SCH-REF-STATUS");
        payment.setStatus(ScheduledPaymentStatus.ACTIVE);
        when(scheduledPaymentRepository.findById(1L)).thenReturn(Optional.of(payment));
        when(scheduledPaymentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        scheduledPaymentService.pauseScheduledPayment(1L);
        assertEquals(ScheduledPaymentStatus.PAUSED, payment.getStatus());

        scheduledPaymentService.resumeScheduledPayment(1L);
        assertEquals(ScheduledPaymentStatus.ACTIVE, payment.getStatus());
    }
}
