package com.cbs.transaction.dto;

import com.cbs.transaction.model.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotNull Long customerId,
        @NotNull Long accountId,
        Long counterpartyAccountId,
        @NotNull TransactionType type,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency,
        @NotBlank @Size(max = 255) String description,
        @NotBlank @Size(max = 64) String reference,
        @NotNull LocalDate valueDate
) {
}
