package com.cbs.interest.job;

import com.cbs.interest.service.InterestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class DailyInterestAccrualJobTest {

    @Mock
    private InterestService interestService;

    private DailyInterestAccrualJob dailyInterestAccrualJob;

    @BeforeEach
    void setUp() {
        dailyInterestAccrualJob = new DailyInterestAccrualJob(interestService);
    }

    @Test
    void executeDailyAccrual_runsSuccessfully() {
        when(interestService.calculateDailyAccrualsForAllAccounts(any(LocalDate.class))).thenReturn(5);

        dailyInterestAccrualJob.executeDailyAccrual();

        verify(interestService).calculateDailyAccrualsForAllAccounts(any(LocalDate.class));
    }

    @Test
    void executeDailyAccrual_handlesExceptionGracefully() {
        doThrow(new RuntimeException("DB Connection failed"))
                .when(interestService).calculateDailyAccrualsForAllAccounts(any(LocalDate.class));

        // Should not throw exception out to the scheduler
        dailyInterestAccrualJob.executeDailyAccrual();

        verify(interestService).calculateDailyAccrualsForAllAccounts(any(LocalDate.class));
    }
}
