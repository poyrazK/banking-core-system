package com.cbs.account.dto;

import com.cbs.account.model.Account;
import com.cbs.account.model.AccountStatus;
import com.cbs.account.model.AccountType;
import com.cbs.account.model.Currency;

import java.math.BigDecimal;

public record AccountResponse(
        Long id,
        Long customerId,
        String accountNumber,
        AccountType accountType,
        Currency currency,
        AccountStatus status,
        BigDecimal balance) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCustomerId(),
                account.getAccountNumber(),
                account.getType(),
                account.getCurrencyCode(),
                account.getStatus(),
                account.getBalance());
    }
}
