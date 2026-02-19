package com.cbs.interest.service;

import com.cbs.common.exception.ApiException;
import com.cbs.interest.dto.CreateInterestConfigRequest;
import com.cbs.interest.dto.InterestAccrualResponse;
import com.cbs.interest.dto.InterestConfigResponse;
import com.cbs.interest.dto.RunAccrualRequest;
import com.cbs.interest.dto.UpdateInterestConfigRequest;
import com.cbs.interest.model.InterestAccrual;
import com.cbs.interest.model.InterestBasis;
import com.cbs.interest.model.InterestConfig;
import com.cbs.interest.model.InterestStatus;
import com.cbs.interest.repository.InterestAccrualRepository;
import com.cbs.interest.repository.InterestConfigRepository;
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
class InterestServiceTest {

    @Mock
    private InterestConfigRepository interestConfigRepository;

    @Mock
    private InterestAccrualRepository interestAccrualRepository;

    private InterestService interestService;

    @BeforeEach
    void setUp() {
        interestService = new InterestService(interestConfigRepository, interestAccrualRepository);
    }

    @Test
    void createConfig_normalizesProductCode() {
        CreateInterestConfigRequest request = new CreateInterestConfigRequest(
                "  sav-01  ",
                BigDecimal.valueOf(12.50),
                InterestBasis.SIMPLE,
                30
        );
        when(interestConfigRepository.existsByProductCode("SAV-01")).thenReturn(false);
        when(interestConfigRepository.save(any(InterestConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InterestConfigResponse response = interestService.createConfig(request);

        assertEquals("SAV-01", response.productCode());
        assertEquals(InterestStatus.ACTIVE, response.status());
    }

    @Test
    void createConfig_throwsWhenProductCodeExists() {
        CreateInterestConfigRequest request = new CreateInterestConfigRequest(
                "SAV-01",
                BigDecimal.valueOf(12.50),
                InterestBasis.SIMPLE,
                30
        );
        when(interestConfigRepository.existsByProductCode("SAV-01")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> interestService.createConfig(request));

        assertEquals("INTEREST_CONFIG_EXISTS", exception.getErrorCode());
    }

    @Test
    void runAccrual_throwsWhenConfigInactive() {
        InterestConfig config = new InterestConfig("SAV-01", BigDecimal.valueOf(10), InterestBasis.SIMPLE, 30);
        config.setStatus(InterestStatus.INACTIVE);
        when(interestConfigRepository.findByProductCode("SAV-01")).thenReturn(Optional.of(config));

        RunAccrualRequest request = new RunAccrualRequest(1L, "sav-01", BigDecimal.valueOf(10000), LocalDate.of(2026, 2, 18));

        ApiException exception = assertThrows(ApiException.class, () -> interestService.runAccrual(request));

        assertEquals("INTEREST_CONFIG_INACTIVE", exception.getErrorCode());
    }

    @Test
    void runAccrual_createsAccrualWithComputedAmount() {
        InterestConfig config = new InterestConfig("SAV-01", BigDecimal.valueOf(12), InterestBasis.SIMPLE, 30);
        when(interestConfigRepository.findByProductCode("SAV-01")).thenReturn(Optional.of(config));
        when(interestAccrualRepository.save(any(InterestAccrual.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InterestAccrualResponse response = interestService.runAccrual(
                new RunAccrualRequest(1L, "sav-01", BigDecimal.valueOf(10000), LocalDate.of(2026, 2, 18))
        );

        assertEquals("SAV-01", response.productCode());
        assertEquals(BigDecimal.valueOf(98.63), response.accruedAmount());
    }

    @Test
    void updateConfig_throwsWhenFrequencyInvalid() {
        ApiException exception = assertThrows(
                ApiException.class,
                () -> interestService.updateConfig(
                        "SAV-01",
                        new UpdateInterestConfigRequest(BigDecimal.valueOf(12), InterestBasis.SIMPLE, 0, InterestStatus.ACTIVE)
                )
        );

        assertEquals("INTEREST_INVALID_FREQUENCY", exception.getErrorCode());
    }
}
