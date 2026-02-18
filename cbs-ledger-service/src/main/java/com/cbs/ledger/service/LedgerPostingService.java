package com.cbs.ledger.service;

import com.cbs.common.exception.ApiException;
import com.cbs.ledger.dto.JournalLineRequest;
import com.cbs.ledger.dto.PostJournalEntryRequest;
import com.cbs.ledger.dto.PostJournalEntryResponse;
import com.cbs.ledger.model.EntryType;
import com.cbs.ledger.model.JournalEntry;
import com.cbs.ledger.model.JournalEntryLine;
import com.cbs.ledger.model.LedgerAccount;
import com.cbs.ledger.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class LedgerPostingService {

    private final JournalEntryRepository journalEntryRepository;
    private final LedgerAccountService ledgerAccountService;

    public LedgerPostingService(JournalEntryRepository journalEntryRepository,
                                LedgerAccountService ledgerAccountService) {
        this.journalEntryRepository = journalEntryRepository;
        this.ledgerAccountService = ledgerAccountService;
    }

    @Transactional
    public PostJournalEntryResponse postEntry(PostJournalEntryRequest request) {
        String reference = request.reference().trim();
        if (journalEntryRepository.existsByReference(reference)) {
            throw new ApiException("LEDGER_REFERENCE_EXISTS", "Reference already posted: " + reference);
        }

        JournalEntry journalEntry = new JournalEntry(reference, request.description().trim(), request.valueDate());
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (JournalLineRequest lineRequest : request.lines()) {
            LedgerAccount account = ledgerAccountService.getActiveAccountByCode(lineRequest.accountCode());
            BigDecimal amount = lineRequest.amount().setScale(4, RoundingMode.HALF_UP);
            if (lineRequest.entryType() == EntryType.DEBIT) {
                totalDebit = totalDebit.add(amount);
            } else {
                totalCredit = totalCredit.add(amount);
            }
            journalEntry.addLine(new JournalEntryLine(account, lineRequest.entryType(), amount));
        }

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new ApiException("LEDGER_UNBALANCED_ENTRY", "Debit and credit totals must be equal");
        }

        JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
        return new PostJournalEntryResponse(savedEntry.getId(), savedEntry.getReference(), totalDebit, totalCredit);
    }
}
