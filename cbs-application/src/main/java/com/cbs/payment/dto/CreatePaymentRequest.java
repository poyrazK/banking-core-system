package com.cbs.payment.dto;

import com.cbs.payment.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePaymentRequest(
        @NotNull Long customerId,
        @NotNull Long sourceAccountId,
        Long destinationAccountId,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotNull PaymentMethod method,
        @NotBlank @Size(max = 64) String reference,
        @NotBlank @Size(max = 255) String description,
        @NotNull LocalDate valueDate
) {
}
