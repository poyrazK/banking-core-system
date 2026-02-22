package com.cbs.interest.job;

import com.cbs.interest.service.InterestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MonthlyInterestCapitalizationJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonthlyInterestCapitalizationJob.class);

    private final InterestService interestService;

    public MonthlyInterestCapitalizationJob(InterestService interestService) {
        this.interestService = interestService;
    }

    /**
     * Runs on the last day of every month at 23:59:59
     */
    @Scheduled(cron = "59 59 23 L * ?")
    public void executeMonthlyCapitalization() {
        LOGGER.info("Starting Monthly Interest Capitalization Job...");
        LocalDate runDate = LocalDate.now();
        try {
            int capitalizedCount = interestService.capitalizeMonthlyAccruals(runDate);
            LOGGER.info("Monthly Interest Capitalization Job completed successfully. Accounts processed: {}",
                    capitalizedCount);
        } catch (Exception e) {
            LOGGER.error("Monthly Interest Capitalization Job failed", e);
        }
    }
}
