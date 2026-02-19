package com.cbs.ledger.dto;

import com.cbs.ledger.model.EntryType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record JournalLineRequest(
        @NotBlank @Size(max = 32) String accountCode,
        @NotNull EntryType entryType,
        @NotNull @Positive @Digits(integer = 15, fraction = 4) BigDecimal amount
) {
}
