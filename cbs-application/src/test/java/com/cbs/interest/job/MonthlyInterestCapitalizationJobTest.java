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
class MonthlyInterestCapitalizationJobTest {

    @Mock
    private InterestService interestService;

    private MonthlyInterestCapitalizationJob capitalizationJob;

    @BeforeEach
    void setUp() {
        capitalizationJob = new MonthlyInterestCapitalizationJob(interestService);
    }

    @Test
    void executeMonthlyCapitalization_runsSuccessfully() {
        when(interestService.capitalizeMonthlyAccruals(any(LocalDate.class))).thenReturn(3);

        capitalizationJob.executeMonthlyCapitalization();

        verify(interestService).capitalizeMonthlyAccruals(any(LocalDate.class));
    }

    @Test
    void executeMonthlyCapitalization_handlesExceptionGracefully() {
        doThrow(new RuntimeException("DB Connection failed"))
                .when(interestService).capitalizeMonthlyAccruals(any(LocalDate.class));

        // Should not throw exception out to the scheduler
        capitalizationJob.executeMonthlyCapitalization();

        verify(interestService).capitalizeMonthlyAccruals(any(LocalDate.class));
    }
}
