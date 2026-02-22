package com.cbs.loan.service;

import com.cbs.account.dto.AccountResponse;
import com.cbs.account.model.AccountStatus;
import com.cbs.account.model.AccountType;
import com.cbs.account.model.Currency;
import com.cbs.account.service.AccountService;
import com.cbs.ledger.dto.PostJournalEntryRequest;
import com.cbs.ledger.dto.PostJournalEntryResponse;
import com.cbs.ledger.service.LedgerPostingService;
import com.cbs.loan.model.AmortizationType;
import com.cbs.loan.model.Loan;
import com.cbs.loan.model.LoanScheduleEntry;
import com.cbs.loan.model.LoanStatus;
import com.cbs.loan.model.LoanType;
import com.cbs.loan.repository.LoanRepository;
import com.cbs.loan.repository.LoanScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanRepaymentServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanScheduleRepository loanScheduleRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private LedgerPostingService ledgerPostingService;

    private LoanRepaymentService loanRepaymentService;

    @BeforeEach
    void setUp() {
        loanRepaymentService = new LoanRepaymentService(loanRepository, loanScheduleRepository, accountService,
                ledgerPostingService);
    }

    @Test
    void processDueInstallments_collectsWhenSufficientBalance() {
        LocalDate processDate = LocalDate.of(2026, 3, 1);
        Loan loan = new Loan(1L, 10L, "LOAN-123", LoanType.PERSONAL, BigDecimal.valueOf(1000), BigDecimal.valueOf(10),
                12, LocalDate.of(2026, 2, 1), LocalDate.of(2027, 2, 1), AmortizationType.FLAT);
        loan.setStatus(LoanStatus.DISBURSED);
        loan.setOutstandingAmount(BigDecimal.valueOf(1000));

        LoanScheduleEntry entry = new LoanScheduleEntry(1L, 1, processDate, BigDecimal.valueOf(80),
                BigDecimal.valueOf(20), BigDecimal.valueOf(100), BigDecimal.valueOf(920));

        when(loanScheduleRepository.findByPaidFalseAndDueDateLessThanEqualOrderByDueDateAsc(processDate))
                .thenReturn(List.of(entry));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        AccountResponse accountRes = new AccountResponse(10L, 1L, "ACC-001", AccountType.SAVINGS, Currency.USD,
                AccountStatus.ACTIVE, BigDecimal.valueOf(500));
        when(accountService.getAccount(10L)).thenReturn(accountRes);

        when(ledgerPostingService.postEntry(any(PostJournalEntryRequest.class)))
                .thenReturn(new PostJournalEntryResponse(1L, "REF", BigDecimal.valueOf(100), BigDecimal.valueOf(100)));

        int processed = loanRepaymentService.processDueInstallments(processDate);

        assertEquals(1, processed);
        assertTrue(entry.isPaid());
        assertEquals(BigDecimal.valueOf(920), loan.getOutstandingAmount());

        verify(accountService).debitBalance(any(), any());
        verify(ledgerPostingService).postEntry(any(PostJournalEntryRequest.class));
    }

    @Test
    void processDueInstallments_skipsWhenInsufficientBalance() {
        LocalDate processDate = LocalDate.of(2026, 3, 1);
        Loan loan = new Loan(1L, 10L, "LOAN-123", LoanType.PERSONAL, BigDecimal.valueOf(1000), BigDecimal.valueOf(10),
                12, LocalDate.of(2026, 2, 1), LocalDate.of(2027, 2, 1), AmortizationType.FLAT);
        loan.setStatus(LoanStatus.DISBURSED);

        LoanScheduleEntry entry = new LoanScheduleEntry(1L, 1, processDate, BigDecimal.valueOf(80),
                BigDecimal.valueOf(20), BigDecimal.valueOf(100), BigDecimal.valueOf(920));

        when(loanScheduleRepository.findByPaidFalseAndDueDateLessThanEqualOrderByDueDateAsc(processDate))
                .thenReturn(List.of(entry));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        // Account balance is 50, but needed is 100
        AccountResponse accountRes = new AccountResponse(10L, 1L, "ACC-001", AccountType.SAVINGS, Currency.USD,
                AccountStatus.ACTIVE, BigDecimal.valueOf(50));
        when(accountService.getAccount(10L)).thenReturn(accountRes);

        int processed = loanRepaymentService.processDueInstallments(processDate);

        assertEquals(1, processed); // Note: it "processed" it (considered it) but didn't execute
        // Actually the code inside loop try-catch finishes successfully without paying
        // so it adds 1 to processed count

        verify(accountService, never()).debitBalance(any(), any());
        verify(ledgerPostingService, never()).postEntry(any(PostJournalEntryRequest.class));
    }
}
