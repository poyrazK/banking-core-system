package com.cbs.ledger.service;

import com.cbs.common.exception.ApiException;
import com.cbs.ledger.dto.BalanceResponse;
import com.cbs.ledger.dto.ReconciliationResponse;
import com.cbs.ledger.model.AccountType;
import com.cbs.ledger.model.EntryType;
import com.cbs.ledger.model.LedgerAccount;
import com.cbs.ledger.repository.JournalEntryLineRepository;
import com.cbs.ledger.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerQueryServiceTest {

    @Mock
    private LedgerAccountService ledgerAccountService;

    @Mock
    private JournalEntryLineRepository journalEntryLineRepository;

    @Mock
    private JournalEntryRepository journalEntryRepository;

    private LedgerQueryService ledgerQueryService;

    @BeforeEach
    void setUp() {
        ledgerQueryService = new LedgerQueryService(ledgerAccountService, journalEntryLineRepository, journalEntryRepository);
    }

    @Test
    void getGlBalance_returnsDebitCreditAndNetBalance() {
        when(ledgerAccountService.getAccountByCode("1000")).thenReturn(new LedgerAccount("1000", "Cash", AccountType.ASSET));
        when(journalEntryLineRepository.sumAmountByAccountCodeAndEntryType("1000", EntryType.DEBIT))
                .thenReturn(new BigDecimal("150.0000"));
        when(journalEntryLineRepository.sumAmountByAccountCodeAndEntryType("1000", EntryType.CREDIT))
                .thenReturn(new BigDecimal("40.0000"));

        BalanceResponse response = ledgerQueryService.getGlBalance("1000");

        assertEquals(new BigDecimal("110.0000"), response.balance());
    }

    @Test
    void reconcile_throwsWhenDateRangeIsInvalid() {
        ApiException exception = assertThrows(ApiException.class,
                () -> ledgerQueryService.reconcile(LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 19)));

        assertEquals("LEDGER_INVALID_DATE_RANGE", exception.getErrorCode());
    }

    @Test
    void reconcile_returnsBalancedSummary() {
        LocalDate fromDate = LocalDate.of(2026, 2, 1);
        LocalDate toDate = LocalDate.of(2026, 2, 28);
        when(journalEntryLineRepository.sumAmountByEntryTypeAndDateRange(EntryType.DEBIT, fromDate, toDate))
                .thenReturn(new BigDecimal("500.0000"));
        when(journalEntryLineRepository.sumAmountByEntryTypeAndDateRange(EntryType.CREDIT, fromDate, toDate))
                .thenReturn(new BigDecimal("500.0000"));
        when(journalEntryRepository.countByValueDateBetween(fromDate, toDate)).thenReturn(7L);

        ReconciliationResponse response = ledgerQueryService.reconcile(fromDate, toDate);

        assertEquals(true, response.balanced());
        assertEquals(7L, response.entryCount());
    }
}