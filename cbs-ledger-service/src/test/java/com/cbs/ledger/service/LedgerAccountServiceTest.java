package com.cbs.ledger.service;

import com.cbs.common.exception.ApiException;
import com.cbs.ledger.dto.AccountResponse;
import com.cbs.ledger.dto.CreateAccountRequest;
import com.cbs.ledger.model.AccountType;
import com.cbs.ledger.model.LedgerAccount;
import com.cbs.ledger.repository.LedgerAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerAccountServiceTest {

    @Mock
    private LedgerAccountRepository ledgerAccountRepository;

    private LedgerAccountService ledgerAccountService;

    @BeforeEach
    void setUp() {
        ledgerAccountService = new LedgerAccountService(ledgerAccountRepository);
    }

    @Test
    void createAccount_normalizesCodeAndName() {
        CreateAccountRequest request = new CreateAccountRequest("  cash-100 ", " Cash account ", AccountType.ASSET);
        when(ledgerAccountRepository.existsByCode("CASH-100")).thenReturn(false);
        when(ledgerAccountRepository.save(any(LedgerAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = ledgerAccountService.createAccount(request);

        assertEquals("CASH-100", response.code());
        assertEquals("Cash account", response.name());
        assertEquals(AccountType.ASSET, response.type());
    }

    @Test
    void createAccount_throwsWhenCodeAlreadyExists() {
        CreateAccountRequest request = new CreateAccountRequest("cash-100", "Cash account", AccountType.ASSET);
        when(ledgerAccountRepository.existsByCode("CASH-100")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> ledgerAccountService.createAccount(request));

        assertEquals("LEDGER_ACCOUNT_EXISTS", exception.getErrorCode());
    }

    @Test
    void listAccounts_returnsOrderedAccounts() {
        when(ledgerAccountRepository.findAllByOrderByCodeAsc()).thenReturn(List.of(
                new LedgerAccount("1000", "Cash", AccountType.ASSET),
                new LedgerAccount("2000", "Payables", AccountType.LIABILITY)
        ));

        List<AccountResponse> result = ledgerAccountService.listAccounts();

        assertEquals(2, result.size());
        assertEquals("1000", result.get(0).code());
    }

    @Test
    void getAccountByCode_throwsWhenMissing() {
        when(ledgerAccountRepository.findByCode("9999")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> ledgerAccountService.getAccountByCode(" 9999 "));

        assertEquals("LEDGER_ACCOUNT_NOT_FOUND", exception.getErrorCode());
    }
}