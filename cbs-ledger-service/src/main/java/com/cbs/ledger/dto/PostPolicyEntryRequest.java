package com.cbs.ledger.dto;

import com.cbs.ledger.model.LedgerOperationType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PostPolicyEntryRequest(
        @NotBlank @Size(max = 64) String reference,
        @NotBlank @Size(max = 255) String description,
        @NotNull LocalDate valueDate,
        @NotNull LedgerOperationType operationType,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
        @NotBlank @Size(max = 64) String accountCode,
        @Size(max = 64) String counterpartyAccountCode
) {
}
