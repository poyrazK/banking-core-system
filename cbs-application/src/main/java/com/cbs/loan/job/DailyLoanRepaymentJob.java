package com.cbs.loan.job;

import com.cbs.loan.service.LoanRepaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailyLoanRepaymentJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyLoanRepaymentJob.class);

    private final LoanRepaymentService loanRepaymentService;

    public DailyLoanRepaymentJob(LoanRepaymentService loanRepaymentService) {
        this.loanRepaymentService = loanRepaymentService;
    }

    /**
     * Runs every day at 01:00:00 to collect due loan installments
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void executeDailyRepayment() {
        LOGGER.info("Starting Daily Loan Repayment Job...");
        try {
            int processedCount = loanRepaymentService.processDueInstallments(LocalDate.now());
            LOGGER.info("Daily Loan Repayment Job completed successfully. Installments processed: {}", processedCount);
        } catch (Exception e) {
            LOGGER.error("Daily Loan Repayment Job failed", e);
        }
    }
}
