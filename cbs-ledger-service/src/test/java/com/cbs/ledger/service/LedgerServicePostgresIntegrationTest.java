package com.cbs.ledger.service;

import com.cbs.ledger.dto.CreateAccountRequest;
import com.cbs.ledger.dto.JournalLineRequest;
import com.cbs.ledger.dto.PostJournalEntryRequest;
import com.cbs.ledger.dto.PostJournalEntryResponse;
import com.cbs.ledger.model.AccountType;
import com.cbs.ledger.model.EntryType;
import com.cbs.ledger.repository.JournalEntryLineRepository;
import com.cbs.ledger.repository.JournalEntryRepository;
import com.cbs.ledger.repository.LedgerAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LedgerServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", () -> System.getProperty(
        "it.db.url",
        "jdbc:postgresql://localhost:55433/cbs_ledger_it"
    ));
    registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
    registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private LedgerAccountService ledgerAccountService;

    @Autowired
    private LedgerPostingService ledgerPostingService;

    @Autowired
    private LedgerAccountRepository ledgerAccountRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private JournalEntryLineRepository journalEntryLineRepository;

    @AfterEach
    void cleanUp() {
        journalEntryLineRepository.deleteAll();
        journalEntryRepository.deleteAll();
        ledgerAccountRepository.deleteAll();
    }

    @Test
    void createAccountPersistsNormalizedCodeInPostgres() {
        var response = ledgerAccountService.createAccount(new CreateAccountRequest(
                " cash-100 ",
                "Cash Account",
                AccountType.ASSET
        ));

        assertEquals("CASH-100", response.code());
        assertTrue(ledgerAccountRepository.existsByCode("CASH-100"));
    }

    @Test
    void postBalancedEntryPersistsJournalLinesInPostgres() {
        ledgerAccountService.createAccount(new CreateAccountRequest("1000", "Cash", AccountType.ASSET));
        ledgerAccountService.createAccount(new CreateAccountRequest("4000", "Revenue", AccountType.INCOME));

        PostJournalEntryResponse response = ledgerPostingService.postEntry(new PostJournalEntryRequest(
                " REF-100 ",
                "Cash sale",
                LocalDate.of(2026, 2, 18),
                List.of(
                        new JournalLineRequest("1000", EntryType.DEBIT, new BigDecimal("120.25")),
                        new JournalLineRequest("4000", EntryType.CREDIT, new BigDecimal("120.25"))
                )
        ));

        assertEquals("REF-100", response.reference());
        assertEquals(new BigDecimal("120.2500"), response.totalDebit());
        assertEquals(1L, journalEntryRepository.countByValueDateBetween(LocalDate.of(2026, 2, 18), LocalDate.of(2026, 2, 18)));
    }
}