package com.cbs.fee.service;

import com.cbs.common.exception.ApiException;
import com.cbs.fee.dto.ChargeFeeRequest;
import com.cbs.fee.dto.CreateFeeConfigRequest;
import com.cbs.fee.dto.FeeChargeResponse;
import com.cbs.fee.dto.FeeConfigResponse;
import com.cbs.fee.model.FeeCharge;
import com.cbs.fee.model.FeeConfig;
import com.cbs.fee.model.FeeStatus;
import com.cbs.fee.model.FeeType;
import com.cbs.fee.repository.FeeChargeRepository;
import com.cbs.fee.repository.FeeConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {

    @Mock
    private FeeConfigRepository feeConfigRepository;

    @Mock
    private FeeChargeRepository feeChargeRepository;

    private FeeService feeService;

    @BeforeEach
    void setUp() {
        feeService = new FeeService(feeConfigRepository, feeChargeRepository);
    }

    @Test
    void createConfig_normalizesFeeCode() {
        CreateFeeConfigRequest request = new CreateFeeConfigRequest(
                "  trn-fee  ",
                "Transfer Fee",
                FeeType.TRANSFER,
                BigDecimal.valueOf(1.00),
                BigDecimal.valueOf(0.50)
        );
        when(feeConfigRepository.existsByFeeCode("TRN-FEE")).thenReturn(false);
        when(feeConfigRepository.save(any(FeeConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FeeConfigResponse response = feeService.createConfig(request);

        assertEquals("TRN-FEE", response.feeCode());
    }

    @Test
    void createConfig_throwsWhenCodeExists() {
        CreateFeeConfigRequest request = new CreateFeeConfigRequest(
                "TRN-FEE",
                "Transfer Fee",
                FeeType.TRANSFER,
                BigDecimal.valueOf(1.00),
                BigDecimal.valueOf(0.50)
        );
        when(feeConfigRepository.existsByFeeCode("TRN-FEE")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> feeService.createConfig(request));

        assertEquals("FEE_CONFIG_EXISTS", exception.getErrorCode());
    }

    @Test
    void chargeFee_throwsWhenConfigInactive() {
        FeeConfig config = new FeeConfig("TRN-FEE", "Transfer Fee", FeeType.TRANSFER, BigDecimal.ONE, BigDecimal.ONE);
        config.setStatus(FeeStatus.INACTIVE);
        when(feeConfigRepository.findByFeeCode("TRN-FEE")).thenReturn(Optional.of(config));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> feeService.chargeFee(new ChargeFeeRequest(1L, "trn-fee", BigDecimal.valueOf(1000), "TRY"))
        );

        assertEquals("FEE_CONFIG_INACTIVE", exception.getErrorCode());
    }

    @Test
    void chargeFee_calculatesFixedPlusPercentage() {
        FeeConfig config = new FeeConfig("TRN-FEE", "Transfer Fee", FeeType.TRANSFER, BigDecimal.valueOf(1.00), BigDecimal.valueOf(0.50));
        when(feeConfigRepository.findByFeeCode("TRN-FEE")).thenReturn(Optional.of(config));
        when(feeChargeRepository.save(any(FeeCharge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FeeChargeResponse response = feeService.chargeFee(
                new ChargeFeeRequest(1L, "trn-fee", BigDecimal.valueOf(1000), "try")
        );

        assertEquals(new BigDecimal("6.00"), response.feeAmount());
        assertEquals("TRY", response.currency());
    }

    @Test
    void getConfig_throwsWhenNotFound() {
        when(feeConfigRepository.findByFeeCode("TRN-FEE")).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> feeService.getConfig("trn-fee"));

        assertEquals("FEE_CONFIG_NOT_FOUND", exception.getErrorCode());
    }
}
