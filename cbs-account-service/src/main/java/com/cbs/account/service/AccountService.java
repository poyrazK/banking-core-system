package com.cbs.account.service;

import com.cbs.account.dto.AccountResponse;
import com.cbs.account.dto.BalanceUpdateRequest;
import com.cbs.account.dto.CreateAccountRequest;
import com.cbs.account.dto.CurrencyResponse;
import com.cbs.account.dto.UpdateAccountStatusRequest;
import com.cbs.account.model.Account;
import com.cbs.account.model.AccountStatus;
import com.cbs.account.repository.AccountRepository;
import com.cbs.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        String accountNumber = normalizeAccountNumber(request.accountNumber());
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new ApiException("ACCOUNT_ALREADY_EXISTS", "Account number already exists");
        }

        BigDecimal initialBalance = request.initialBalance() == null ? BigDecimal.ZERO : request.initialBalance();
        Account account = new Account(request.customerId(), accountNumber, request.accountType(), request.currency(),
                initialBalance);
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse getAccount(Long accountId) {
        return AccountResponse.from(findAccount(accountId));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> listAccounts(Long customerId) {
        List<Account> accounts = customerId == null
                ? accountRepository.findAll()
                : accountRepository.findByCustomerIdOrderByIdDesc(customerId);
        return accounts.stream().map(AccountResponse::from).toList();
    }

    @Transactional
    public AccountResponse creditBalance(Long accountId, BalanceUpdateRequest request) {
        Account account = findAccount(accountId);
        ensureBalanceUpdateAllowed(account);
        ensureCurrencyMatches(account, request.currency());

        account.setBalance(account.getBalance().add(request.amount()));
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse debitBalance(Long accountId, BalanceUpdateRequest request) {
        Account account = findAccount(accountId);
        ensureBalanceUpdateAllowed(account);
        ensureCurrencyMatches(account, request.currency());

        BigDecimal amount = request.amount();
        if (account.getBalance().compareTo(amount) < 0) {
            throw new ApiException("INSUFFICIENT_BALANCE", "Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional
    public AccountResponse updateStatus(Long accountId, UpdateAccountStatusRequest request) {
        Account account = findAccount(accountId);
        if (account.getStatus() == AccountStatus.CLOSED && request.status() != AccountStatus.CLOSED) {
            throw new ApiException("ACCOUNT_CLOSED", "Closed account cannot be reactivated");
        }

        account.setStatus(request.status());
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public CurrencyResponse getAccountCurrency(Long accountId) {
        Account account = findAccount(accountId);
        return new CurrencyResponse(account.getId(), account.getCurrencyCode());
    }

    private Account findAccount(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ApiException("ACCOUNT_NOT_FOUND", "Account not found"));
    }

    private void ensureBalanceUpdateAllowed(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new ApiException("ACCOUNT_NOT_ACTIVE", "Balance updates are allowed only for active accounts");
        }
    }

    private void ensureCurrencyMatches(Account account, com.cbs.account.model.Currency requestCurrency) {
        if (requestCurrency != null && requestCurrency != account.getCurrencyCode()) {
            throw new ApiException("CURRENCY_MISMATCH",
                    "Expected " + account.getCurrencyCode() + " but received " + requestCurrency);
        }
    }

    private String normalizeAccountNumber(String accountNumber) {
        return accountNumber.trim().toUpperCase();
    }
}
