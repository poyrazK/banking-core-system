package com.cbs.ledger.service;

import com.cbs.common.exception.ApiException;
import com.cbs.ledger.dto.JournalLineRequest;
import com.cbs.ledger.dto.PostJournalEntryRequest;
import com.cbs.ledger.dto.PostJournalEntryResponse;
import com.cbs.ledger.model.AccountType;
import com.cbs.ledger.model.EntryType;
import com.cbs.ledger.model.JournalEntry;
import com.cbs.ledger.model.LedgerAccount;
import com.cbs.ledger.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerPostingServiceTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private LedgerAccountService ledgerAccountService;

    private LedgerPostingService ledgerPostingService;

    @BeforeEach
    void setUp() {
        ledgerPostingService = new LedgerPostingService(journalEntryRepository, ledgerAccountService);
    }

    @Test
    void postEntry_returnsTotalsForBalancedEntry() {
        PostJournalEntryRequest request = new PostJournalEntryRequest(
                " REF-1 ",
                " Cash sale ",
                LocalDate.of(2026, 2, 18),
                List.of(
                        new JournalLineRequest("1000", EntryType.DEBIT, new BigDecimal("100.12")),
                        new JournalLineRequest("4000", EntryType.CREDIT, new BigDecimal("100.12"))
                )
        );
        when(journalEntryRepository.existsByReference("REF-1")).thenReturn(false);
        when(ledgerAccountService.getActiveAccountByCode("1000")).thenReturn(new LedgerAccount("1000", "Cash", AccountType.ASSET));
        when(ledgerAccountService.getActiveAccountByCode("4000")).thenReturn(new LedgerAccount("4000", "Revenue", AccountType.INCOME));
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostJournalEntryResponse response = ledgerPostingService.postEntry(request);

        assertEquals("REF-1", response.reference());
        assertEquals(new BigDecimal("100.1200"), response.totalDebit());
        assertEquals(new BigDecimal("100.1200"), response.totalCredit());
    }

    @Test
    void postEntry_throwsWhenEntryIsUnbalanced() {
        PostJournalEntryRequest request = new PostJournalEntryRequest(
                "REF-2",
                "Invalid",
                LocalDate.of(2026, 2, 18),
                List.of(
                        new JournalLineRequest("1000", EntryType.DEBIT, new BigDecimal("100.00")),
                        new JournalLineRequest("4000", EntryType.CREDIT, new BigDecimal("99.99"))
                )
        );
        when(journalEntryRepository.existsByReference("REF-2")).thenReturn(false);
        when(ledgerAccountService.getActiveAccountByCode("1000")).thenReturn(new LedgerAccount("1000", "Cash", AccountType.ASSET));
        when(ledgerAccountService.getActiveAccountByCode("4000")).thenReturn(new LedgerAccount("4000", "Revenue", AccountType.INCOME));

        ApiException exception = assertThrows(ApiException.class, () -> ledgerPostingService.postEntry(request));

        assertEquals("LEDGER_UNBALANCED_ENTRY", exception.getErrorCode());
    }
}