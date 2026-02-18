package com.cbs.ledger.service;

import com.cbs.common.exception.ApiException;
import com.cbs.ledger.dto.BalanceResponse;
import com.cbs.ledger.dto.ReconciliationResponse;
import com.cbs.ledger.model.EntryType;
import com.cbs.ledger.model.LedgerAccount;
import com.cbs.ledger.repository.JournalEntryLineRepository;
import com.cbs.ledger.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class LedgerQueryService {

    private final LedgerAccountService ledgerAccountService;
    private final JournalEntryLineRepository journalEntryLineRepository;
    private final JournalEntryRepository journalEntryRepository;

    public LedgerQueryService(LedgerAccountService ledgerAccountService,
                              JournalEntryLineRepository journalEntryLineRepository,
                              JournalEntryRepository journalEntryRepository) {
        this.ledgerAccountService = ledgerAccountService;
        this.journalEntryLineRepository = journalEntryLineRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getGlBalance(String accountCode) {
        LedgerAccount account = ledgerAccountService.getAccountByCode(accountCode);
        BigDecimal totalDebit = journalEntryLineRepository
                .sumAmountByAccountCodeAndEntryType(account.getCode(), EntryType.DEBIT);
        BigDecimal totalCredit = journalEntryLineRepository
                .sumAmountByAccountCodeAndEntryType(account.getCode(), EntryType.CREDIT);
        return new BalanceResponse(account.getCode(), totalDebit, totalCredit, totalDebit.subtract(totalCredit));
    }

    @Transactional(readOnly = true)
    public ReconciliationResponse reconcile(LocalDate fromDate, LocalDate toDate) {
        if (fromDate.isAfter(toDate)) {
            throw new ApiException("LEDGER_INVALID_DATE_RANGE", "fromDate must be before or equal to toDate");
        }

        BigDecimal totalDebit = journalEntryLineRepository
                .sumAmountByEntryTypeAndDateRange(EntryType.DEBIT, fromDate, toDate);
        BigDecimal totalCredit = journalEntryLineRepository
                .sumAmountByEntryTypeAndDateRange(EntryType.CREDIT, fromDate, toDate);
        long entryCount = journalEntryRepository.countByValueDateBetween(fromDate, toDate);

        return new ReconciliationResponse(
                fromDate,
                toDate,
                totalDebit,
                totalCredit,
                totalDebit.compareTo(totalCredit) == 0,
                entryCount
        );
    }
}
