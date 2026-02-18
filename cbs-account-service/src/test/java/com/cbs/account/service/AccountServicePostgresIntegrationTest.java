package com.cbs.account.service;

import com.cbs.account.dto.AccountResponse;
import com.cbs.account.dto.BalanceUpdateRequest;
import com.cbs.account.dto.CreateAccountRequest;
import com.cbs.account.model.Account;
import com.cbs.account.model.AccountType;
import com.cbs.account.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AccountServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", () -> System.getProperty(
        "it.db.url",
        "jdbc:postgresql://localhost:55432/cbs_account_it"
    ));
    registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
    registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    void cleanUp() {
        accountRepository.deleteAll();
    }

    @Test
    void createAccountPersistsNormalizedAccountNumberInPostgres() {
        CreateAccountRequest request = new CreateAccountRequest(
                101L,
                "  tr-acc-0001  ",
                AccountType.SAVINGS,
                new BigDecimal("150.50")
        );

        AccountResponse response = accountService.createAccount(request);

        assertEquals("TR-ACC-0001", response.accountNumber());
        assertTrue(accountRepository.existsByAccountNumber("TR-ACC-0001"));
    }

    @Test
    void creditAndDebitBalanceArePersistedInPostgres() {
        AccountResponse created = accountService.createAccount(new CreateAccountRequest(
                102L,
                "TR-ACC-0002",
                AccountType.CHECKING,
                new BigDecimal("200.00")
        ));

        accountService.creditBalance(created.id(), new BalanceUpdateRequest(new BigDecimal("50.25")));
        AccountResponse debited = accountService.debitBalance(created.id(), new BalanceUpdateRequest(new BigDecimal("20.00")));

        assertEquals(new BigDecimal("230.25"), debited.balance());
        Account persisted = accountRepository.findById(created.id()).orElseThrow();
        assertEquals(new BigDecimal("230.25"), persisted.getBalance());
    }
}