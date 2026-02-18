package com.cbs.ledger.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record PostJournalEntryRequest(
        @NotBlank @Size(max = 64) String reference,
        @NotBlank @Size(max = 255) String description,
        @NotNull LocalDate valueDate,
        @NotNull @Size(min = 2) List<@Valid JournalLineRequest> lines
) {
}
