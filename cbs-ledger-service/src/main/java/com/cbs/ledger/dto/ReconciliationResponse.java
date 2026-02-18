package com.cbs.ledger.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReconciliationResponse(
        LocalDate fromDate,
        LocalDate toDate,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        boolean balanced,
        long entryCount
) {
}
