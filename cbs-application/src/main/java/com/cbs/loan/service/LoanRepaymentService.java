package com.cbs.loan.service;

import com.cbs.account.dto.AccountResponse;
import com.cbs.account.dto.BalanceUpdateRequest;
import com.cbs.account.service.AccountService;
import com.cbs.common.exception.ApiException;
import com.cbs.ledger.dto.JournalLineRequest;
import com.cbs.ledger.dto.PostJournalEntryRequest;
import com.cbs.ledger.model.EntryType;
import com.cbs.ledger.service.LedgerPostingService;
import com.cbs.loan.model.Loan;
import com.cbs.loan.model.LoanScheduleEntry;
import com.cbs.loan.model.LoanStatus;
import com.cbs.loan.repository.LoanRepository;
import com.cbs.loan.repository.LoanScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoanRepaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoanRepaymentService.class);
    private static final String LOAN_ASSET_ACCOUNT = "LOAN-ASSET";
    private static final String INTEREST_INCOME_ACCOUNT = "INTEREST-INCOME";

    private final LoanRepository loanRepository;
    private final LoanScheduleRepository loanScheduleRepository;
    private final AccountService accountService;
    private final LedgerPostingService ledgerPostingService;

    public LoanRepaymentService(LoanRepository loanRepository,
            LoanScheduleRepository loanScheduleRepository,
            AccountService accountService,
            LedgerPostingService ledgerPostingService) {
        this.loanRepository = loanRepository;
        this.loanScheduleRepository = loanScheduleRepository;
        this.accountService = accountService;
        this.ledgerPostingService = ledgerPostingService;
    }

    @Transactional
    public int processDueInstallments(LocalDate processDate) {
        int processedCount = 0;
        List<LoanScheduleEntry> dueEntries = loanScheduleRepository
                .findByPaidFalseAndDueDateLessThanEqualOrderByDueDateAsc(processDate);

        for (LoanScheduleEntry entry : dueEntries) {
            try {
                processSingleInstallment(entry, processDate);
                processedCount++;
            } catch (Exception e) {
                // Log and continue to the next entry so one failure doesn't stop the batch
                LOGGER.error("Failed to process loan schedule entry ID: " + entry.getId(), e);
            }
        }
        return processedCount;
    }

    private void processSingleInstallment(LoanScheduleEntry entry, LocalDate processDate) {
        Loan loan = loanRepository.findById(entry.getLoanId())
                .orElseThrow(() -> new ApiException("LOAN_NOT_FOUND", "Loan not found: " + entry.getLoanId()));

        if (loan.getStatus() != LoanStatus.DISBURSED) {
            LOGGER.warn("Loan {} is not active. Skipping installment {}", loan.getLoanNumber(),
                    entry.getInstallmentNumber());
            return;
        }

        AccountResponse accountRes = accountService.getAccount(loan.getAccountId());
        BigDecimal dueAmount = entry.getTotalPayment();

        if (accountRes.balance().compareTo(dueAmount) < 0) {
            LOGGER.info("Insufficient funds for Loan {} installment {}. Required: {}, Available: {}",
                    loan.getLoanNumber(), entry.getInstallmentNumber(), dueAmount, accountRes.balance());
            return; // Skip and try again tomorrow
        }

        // 1. Debit customer account
        accountService.debitBalance(accountRes.id(), new BalanceUpdateRequest(dueAmount, accountRes.currency()));

        // 2. Reduce loan outstanding balance
        BigDecimal remaining = loan.getOutstandingAmount().subtract(entry.getPrincipalAmount());
        loan.setOutstandingAmount(remaining);
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus(LoanStatus.CLOSED);
        }
        loanRepository.save(loan);

        // 3. Mark schedule entry as paid
        entry.setPaid(true);
        loanScheduleRepository.save(entry);

        // 4. Post Ledger Entry
        String reference = "LOAN-REP-" + loan.getLoanNumber() + "-" + entry.getInstallmentNumber();
        String description = "Loan Repayment Inst " + entry.getInstallmentNumber() + " for " + loan.getLoanNumber();

        List<JournalLineRequest> lines = List.of(
                new JournalLineRequest(accountRes.accountNumber(), EntryType.DEBIT, entry.getTotalPayment()),
                new JournalLineRequest(LOAN_ASSET_ACCOUNT, EntryType.CREDIT, entry.getPrincipalAmount()),
                new JournalLineRequest(INTEREST_INCOME_ACCOUNT, EntryType.CREDIT, entry.getInterestAmount()));

        ledgerPostingService.postEntry(new PostJournalEntryRequest(reference, description, processDate, lines));
        LOGGER.info("Successfully processed loan {} installment {}", loan.getLoanNumber(),
                entry.getInstallmentNumber());
    }
}
