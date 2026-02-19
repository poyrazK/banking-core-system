package com.cbs.ledger.dto;

import java.math.BigDecimal;

public record PostJournalEntryResponse(
        Long entryId,
        String reference,
        BigDecimal totalDebit,
        BigDecimal totalCredit
) {
}
