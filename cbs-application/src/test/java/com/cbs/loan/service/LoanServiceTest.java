package com.cbs.loan.service;

import com.cbs.common.exception.ApiException;
import com.cbs.loan.dto.CreateLoanRequest;
import com.cbs.loan.dto.LoanDecisionRequest;
import com.cbs.loan.dto.LoanRepaymentRequest;
import com.cbs.loan.dto.LoanResponse;
import com.cbs.loan.model.Loan;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanScheduleRepository loanScheduleRepository;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanRepository, loanScheduleRepository);
    }

    @Test
    void createLoan_normalizesLoanNumber() {
        CreateLoanRequest request = new CreateLoanRequest(
                1L,
                10L,
                "  loan-001  ",
                LoanType.PERSONAL,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(12.5),
                12,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2027, 2, 1));
        when(loanRepository.existsByLoanNumber("LOAN-001")).thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.createLoan(request);

        assertEquals("LOAN-001", response.loanNumber());
        assertEquals(LoanStatus.APPLIED, response.status());
    }

    @Test
    void createLoan_throwsWhenLoanNumberExists() {
        CreateLoanRequest request = new CreateLoanRequest(
                1L,
                10L,
                "LOAN-001",
                LoanType.PERSONAL,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(12.5),
                12,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2027, 2, 1));
        when(loanRepository.existsByLoanNumber("LOAN-001")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> loanService.createLoan(request));

        assertEquals("LOAN_NUMBER_EXISTS", exception.getErrorCode());
    }

    @Test
    void approveLoan_throwsWhenStatusIsNotApplied() {
        Loan loan = createLoanWithStatus(LoanStatus.REJECTED);
        when(loanRepository.findById(11L)).thenReturn(Optional.of(loan));

        ApiException exception = assertThrows(ApiException.class, () -> loanService.approveLoan(11L));

        assertEquals("LOAN_NOT_APPLIED", exception.getErrorCode());
    }

    @Test
    void disburseLoan_setsOutstandingAmountToPrincipal() {
        Loan loan = createLoanWithStatus(LoanStatus.APPROVED);
        when(loanRepository.findById(12L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(loanScheduleRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.disburseLoan(12L);

        assertEquals(LoanStatus.DISBURSED, response.status());
        assertEquals(BigDecimal.valueOf(10000), response.outstandingAmount());
    }

    @Test
    void repayLoan_closesLoanWhenOutstandingBecomesZero() {
        Loan loan = createLoanWithStatus(LoanStatus.DISBURSED);
        loan.setOutstandingAmount(BigDecimal.valueOf(300));
        when(loanRepository.findById(13L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.repayLoan(13L, new LoanRepaymentRequest(BigDecimal.valueOf(300)));

        assertEquals(BigDecimal.ZERO, response.outstandingAmount());
        assertEquals(LoanStatus.CLOSED, response.status());
    }

    @Test
    void rejectLoan_setsRejectionReasonTrimmed() {
        Loan loan = createLoanWithStatus(LoanStatus.APPLIED);
        when(loanRepository.findById(14L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.rejectLoan(14L, new LoanDecisionRequest("  low score  "));

        assertEquals(LoanStatus.REJECTED, response.status());
        assertEquals("low score", response.decisionReason());
    }

    private Loan createLoanWithStatus(LoanStatus status) {
        Loan loan = new Loan(
                1L,
                10L,
                "LOAN-100",
                LoanType.PERSONAL,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(12.5),
                12,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2027, 2, 1));
        loan.setStatus(status);
        return loan;
    }
}
