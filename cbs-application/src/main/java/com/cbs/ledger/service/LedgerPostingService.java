package com.cbs.ledger.service;

import com.cbs.common.exception.ApiException;
import com.cbs.ledger.dto.JournalLineRequest;
import com.cbs.ledger.dto.PostJournalEntryRequest;
import com.cbs.ledger.dto.PostPolicyEntryRequest;
import com.cbs.ledger.dto.PostJournalEntryResponse;
import com.cbs.ledger.model.EntryType;
import com.cbs.ledger.model.JournalEntry;
import com.cbs.ledger.model.JournalEntryLine;
import com.cbs.ledger.model.LedgerOperationType;
import com.cbs.ledger.model.LedgerAccount;
import com.cbs.ledger.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class LedgerPostingService {

    private static final String PAYMENT_CLEARING_ACCOUNT = "PAYMENT-CLEARING";
    private static final String CASH_SETTLEMENT_ACCOUNT = "CASH-SETTLEMENT";
    private static final String FEE_INCOME_ACCOUNT = "FEE-INCOME";
    private static final String INTEREST_EXPENSE_ACCOUNT = "INTEREST-EXPENSE";

    private final JournalEntryRepository journalEntryRepository;
    private final LedgerAccountService ledgerAccountService;

    public LedgerPostingService(JournalEntryRepository journalEntryRepository,
            LedgerAccountService ledgerAccountService) {
        this.journalEntryRepository = journalEntryRepository;
        this.ledgerAccountService = ledgerAccountService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PostJournalEntryResponse postPolicyEntry(PostPolicyEntryRequest request) {
        String accountCode = request.accountCode().trim().toUpperCase();
        String counterpartyCode = normalizeCounterpartyCode(request.counterpartyAccountCode());
        LedgerOperationType operationType = request.operationType();

        return switch (operationType) {
            case PAYMENT -> postEntry(new PostJournalEntryRequest(
                    request.reference(),
                    request.description(),
                    request.valueDate(),
                    List.of(
                            new JournalLineRequest(accountCode, EntryType.DEBIT, request.amount()),
                            new JournalLineRequest(resolveOrDefault(counterpartyCode, PAYMENT_CLEARING_ACCOUNT),
                                    EntryType.CREDIT, request.amount()))));
            case TRANSFER -> postEntry(new PostJournalEntryRequest(
                    request.reference(),
                    request.description(),
                    request.valueDate(),
                    List.of(
                            new JournalLineRequest(accountCode, EntryType.DEBIT, request.amount()),
                            new JournalLineRequest(requireCounterparty(counterpartyCode, operationType),
                                    EntryType.CREDIT, request.amount()))));
            case DEPOSIT -> postEntry(new PostJournalEntryRequest(
                    request.reference(),
                    request.description(),
                    request.valueDate(),
                    List.of(
                            new JournalLineRequest(accountCode, EntryType.DEBIT, request.amount()),
                            new JournalLineRequest(CASH_SETTLEMENT_ACCOUNT, EntryType.CREDIT, request.amount()))));
            case WITHDRAWAL -> postEntry(new PostJournalEntryRequest(
                    request.reference(),
                    request.description(),
                    request.valueDate(),
                    List.of(
                            new JournalLineRequest(CASH_SETTLEMENT_ACCOUNT, EntryType.DEBIT, request.amount()),
                            new JournalLineRequest(accountCode, EntryType.CREDIT, request.amount()))));
            case FEE -> postEntry(new PostJournalEntryRequest(
                    request.reference(),
                    request.description(),
                    request.valueDate(),
                    List.of(
                            new JournalLineRequest(accountCode, EntryType.DEBIT, request.amount()),
                            new JournalLineRequest(FEE_INCOME_ACCOUNT, EntryType.CREDIT, request.amount()))));
            case INTEREST -> postEntry(new PostJournalEntryRequest(
                    request.reference(),
                    request.description(),
                    request.valueDate(),
                    List.of(
                            new JournalLineRequest(INTEREST_EXPENSE_ACCOUNT, EntryType.DEBIT, request.amount()),
                            new JournalLineRequest(accountCode, EntryType.CREDIT, request.amount()))));
        };
    }

    private String normalizeCounterpartyCode(String counterpartyAccountCode) {
        if (counterpartyAccountCode == null || counterpartyAccountCode.isBlank()) {
            return null;
        }
        return counterpartyAccountCode.trim().toUpperCase();
    }

    private String requireCounterparty(String counterpartyCode, LedgerOperationType operationType) {
        if (counterpartyCode == null) {
            throw new ApiException(
                    "LEDGER_POLICY_COUNTERPARTY_REQUIRED",
                    "Counterparty account code is required for operation type: " + operationType);
        }
        return counterpartyCode;
    }

    private String resolveOrDefault(String accountCode, String defaultAccountCode) {
        return accountCode != null ? accountCode : defaultAccountCode;
    }
}
