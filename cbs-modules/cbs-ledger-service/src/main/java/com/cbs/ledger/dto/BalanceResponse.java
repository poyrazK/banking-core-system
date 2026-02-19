package com.cbs.ledger.dto;

import java.math.BigDecimal;

public record BalanceResponse(
        String accountCode,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        BigDecimal balance
) {
}
