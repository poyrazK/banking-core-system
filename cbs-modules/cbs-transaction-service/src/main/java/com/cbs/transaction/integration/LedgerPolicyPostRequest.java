package com.cbs.transaction.integration;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerPolicyPostRequest(
        String reference,
        String description,
        LocalDate valueDate,
        String operationType,
        BigDecimal amount,
        String accountCode,
        String counterpartyAccountCode
) {
}
