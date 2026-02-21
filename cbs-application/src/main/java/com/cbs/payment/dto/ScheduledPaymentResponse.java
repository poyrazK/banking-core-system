package com.cbs.payment.dto;

import com.cbs.payment.model.PaymentMethod;
import com.cbs.payment.model.ScheduleFrequency;
import com.cbs.payment.model.ScheduledPayment;
import com.cbs.payment.model.ScheduledPaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScheduledPaymentResponse(
        Long id,
        Long customerId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String currency,
        PaymentMethod method,
        String description,
        ScheduleFrequency frequency,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate nextExecutionDate,
        LocalDate lastExecutedDate,
        ScheduledPaymentStatus status,
        int executionCount,
        int failureCount,
        String lastFailureReason,
        String reference) {
    public static ScheduledPaymentResponse from(ScheduledPayment entity) {
        return new ScheduledPaymentResponse(
                entity.getId(),
                entity.getCustomerId(),
                entity.getSourceAccountId(),
                entity.getDestinationAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                entity.getMethod(),
                entity.getDescription(),
                entity.getFrequency(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getNextExecutionDate(),
                entity.getLastExecutedDate(),
                entity.getStatus(),
                entity.getExecutionCount(),
                entity.getFailureCount(),
                entity.getLastFailureReason(),
                entity.getReference());
    }
}
