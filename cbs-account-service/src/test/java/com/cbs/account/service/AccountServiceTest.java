package com.cbs.account.service;

import com.cbs.account.dto.AccountResponse;
import com.cbs.account.dto.BalanceUpdateRequest;
import com.cbs.account.dto.CreateAccountRequest;
import com.cbs.account.dto.UpdateAccountStatusRequest;
import com.cbs.account.model.Account;
import com.cbs.account.model.AccountStatus;
import com.cbs.account.model.AccountType;
import com.cbs.account.model.Currency;
import com.cbs.account.repository.AccountRepository;
import com.cbs.common.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(accountRepository);
    }

    @Test
    void createAccount_normalizesAccountNumber_andUsesZeroWhenInitialBalanceIsNull() {
        CreateAccountRequest request = new CreateAccountRequest(1L, "  tr001  ", AccountType.SAVINGS, Currency.TRY,
                null);
        when(accountRepository.existsByAccountNumber("TR001")).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.createAccount(request);

        assertEquals("TR001", response.accountNumber());
        assertEquals(BigDecimal.ZERO, response.balance());
        assertEquals(AccountStatus.ACTIVE, response.status());
    }

    @Test
    void createAccount_throwsWhenAccountNumberExists() {
        CreateAccountRequest request = new CreateAccountRequest(1L, "TR001", AccountType.SAVINGS, Currency.TRY,
                BigDecimal.TEN);
        when(accountRepository.existsByAccountNumber("TR001")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> accountService.createAccount(request));

        assertEquals("ACCOUNT_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals("Account number already exists", exception.getMessage());
    }

    @Test
    void creditBalance_throwsWhenAccountNotActive() {
        Account account = new Account(1L, "TR001", AccountType.CHECKING, Currency.TRY, BigDecimal.valueOf(100));
        account.setStatus(AccountStatus.FROZEN);
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> accountService.creditBalance(10L, new BalanceUpdateRequest(BigDecimal.valueOf(20))));

        assertEquals("ACCOUNT_NOT_ACTIVE", exception.getErrorCode());
    }

    @Test
    void debitBalance_throwsWhenInsufficientBalance() {
        Account account = new Account(1L, "TR001", AccountType.CHECKING, Currency.TRY, BigDecimal.valueOf(50));
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> accountService.debitBalance(11L, new BalanceUpdateRequest(BigDecimal.valueOf(75))));

        assertEquals("INSUFFICIENT_BALANCE", exception.getErrorCode());
        assertEquals("Insufficient balance", exception.getMessage());
    }

    @Test
    void creditBalance_throwsWhenCurrencyMismatch() {
        Account account = new Account(1L, "TR001", AccountType.CHECKING, Currency.TRY, BigDecimal.valueOf(100));
        when(accountRepository.findById(10L)).thenReturn(Optional.of(account));

        BalanceUpdateRequest request = new BalanceUpdateRequest(BigDecimal.valueOf(20), Currency.USD);
        ApiException exception = assertThrows(
                ApiException.class,
                () -> accountService.creditBalance(10L, request));

        assertEquals("CURRENCY_MISMATCH", exception.getErrorCode());
        assertEquals("Expected TRY but received USD", exception.getMessage());
    }

    @Test
    void debitBalance_succeedsWhenCurrencyIsNullForBackwardCompatibility() {
        Account account = new Account(1L, "TR001", AccountType.CHECKING, Currency.TRY, BigDecimal.valueOf(100));
        when(accountRepository.findById(11L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BalanceUpdateRequest request = new BalanceUpdateRequest(BigDecimal.valueOf(20), null);
        AccountResponse response = accountService.debitBalance(11L, request);

        assertEquals(0, new BigDecimal("80.00").compareTo(response.balance()));
    }

    @Test
    void getAccountCurrency_returnsCorrectCurrency() {
        Account account = new Account(1L, "TR001", AccountType.SAVINGS, Currency.EUR, BigDecimal.ZERO);
        account.setId(50L);
        when(accountRepository.findById(50L)).thenReturn(Optional.of(account));

        var response = accountService.getAccountCurrency(50L);

        assertEquals(50L, response.accountId());
        assertEquals(Currency.EUR, response.currency());
    }

    @Test
    void updateStatus_throwsWhenReactivatingClosedAccount() {
        Account account = new Account(1L, "TR001", AccountType.CURRENT, Currency.TRY, BigDecimal.ZERO);
        account.setStatus(AccountStatus.CLOSED);
        when(accountRepository.findById(12L)).thenReturn(Optional.of(account));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> accountService.updateStatus(12L, new UpdateAccountStatusRequest(AccountStatus.ACTIVE)));

        assertEquals("ACCOUNT_CLOSED", exception.getErrorCode());
        assertEquals("Closed account cannot be reactivated", exception.getMessage());
    }
}
