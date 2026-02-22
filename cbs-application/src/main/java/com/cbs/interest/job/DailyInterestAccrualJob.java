package com.cbs.interest.job;

import com.cbs.interest.service.InterestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DailyInterestAccrualJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyInterestAccrualJob.class);

    private final InterestService interestService;

    public DailyInterestAccrualJob(InterestService interestService) {
        this.interestService = interestService;
    }

    /**
     * Runs every day at 23:59:59 to accrue end-of-day interest
     */
    @Scheduled(cron = "59 59 23 * * *")
    public void executeDailyAccrual() {
        LOGGER.info("Starting Daily Interest Accrual Job...");
        try {
            int accruedCount = interestService.calculateDailyAccrualsForAllAccounts(LocalDate.now());
            LOGGER.info("Daily Interest Accrual Job completed successfully. Accounts processed: {}", accruedCount);
        } catch (Exception e) {
            LOGGER.error("Daily Interest Accrual Job failed", e);
        }
    }
}
