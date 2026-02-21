package com.cbs.transaction.integration;

import com.cbs.account.service.AccountService;
import org.springframework.stereotype.Component;

@Component
public class DirectAccountClient implements AccountClient {

    private final AccountService accountService;

    public DirectAccountClient(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public String getAccountCurrency(Long accountId) {
        return accountService.getAccountCurrency(accountId)
                .currency().name();
    }
}
