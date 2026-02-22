package com.cbs.loan.job;

import com.cbs.loan.service.LoanRepaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyLoanRepaymentJobTest {

    @Mock
    private LoanRepaymentService loanRepaymentService;

    private DailyLoanRepaymentJob dailyLoanRepaymentJob;

    @BeforeEach
    void setUp() {
        dailyLoanRepaymentJob = new DailyLoanRepaymentJob(loanRepaymentService);
    }

    @Test
    void executeDailyRepayment_runsSuccessfully() {
        when(loanRepaymentService.processDueInstallments(any(LocalDate.class))).thenReturn(5);

        dailyLoanRepaymentJob.executeDailyRepayment();

        verify(loanRepaymentService).processDueInstallments(any(LocalDate.class));
    }

    @Test
    void executeDailyRepayment_handlesExceptionGracefully() {
        doThrow(new RuntimeException("DB Connection failed"))
                .when(loanRepaymentService).processDueInstallments(any(LocalDate.class));

        // Should not throw exception out to the scheduler
        dailyLoanRepaymentJob.executeDailyRepayment();

        verify(loanRepaymentService).processDueInstallments(any(LocalDate.class));
    }
}
