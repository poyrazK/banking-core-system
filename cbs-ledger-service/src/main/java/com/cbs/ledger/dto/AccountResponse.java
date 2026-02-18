package com.cbs.ledger.dto;

import com.cbs.ledger.model.AccountType;
import com.cbs.ledger.model.LedgerAccount;

public record AccountResponse(
        Long id,
        String code,
        String name,
        AccountType type,
        boolean active
) {
    public static AccountResponse fromEntity(LedgerAccount ledgerAccount) {
        return new AccountResponse(
                ledgerAccount.getId(),
                ledgerAccount.getCode(),
                ledgerAccount.getName(),
                ledgerAccount.getType(),
                ledgerAccount.isActive()
        );
    }
}
