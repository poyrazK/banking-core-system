package com.cbs.payment.dto;

import com.cbs.payment.model.Payment;
import com.cbs.payment.model.PaymentMethod;
import com.cbs.payment.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(
        Long id,
        Long customerId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount,
        String currency,
        PaymentMethod method,
        PaymentStatus status,
        String reference,
        String description,
        LocalDate valueDate,
        String failureReason
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getCustomerId(),
                payment.getSourceAccountId(),
                payment.getDestinationAccountId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getReference(),
                payment.getDescription(),
                payment.getValueDate(),
                payment.getFailureReason()
        );
    }
}
