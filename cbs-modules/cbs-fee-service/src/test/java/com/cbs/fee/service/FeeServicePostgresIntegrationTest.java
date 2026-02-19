package com.cbs.fee.service;

import com.cbs.fee.dto.ChargeFeeRequest;
import com.cbs.fee.dto.CreateFeeConfigRequest;
import com.cbs.fee.dto.FeeChargeResponse;
import com.cbs.fee.dto.FeeConfigResponse;
import com.cbs.fee.model.FeeType;
import com.cbs.fee.repository.FeeChargeRepository;
import com.cbs.fee.repository.FeeConfigRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class FeeServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55439/cbs_fee_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private FeeService feeService;

    @Autowired
    private FeeConfigRepository feeConfigRepository;

    @Autowired
    private FeeChargeRepository feeChargeRepository;

    @AfterEach
    void cleanUp() {
        feeChargeRepository.deleteAll();
        feeConfigRepository.deleteAll();
    }

    @Test
    void createConfigPersistsNormalizedFeeCodeInPostgres() {
        FeeConfigResponse response = feeService.createConfig(new CreateFeeConfigRequest(
                "  trn-fee  ",
                "Transfer Fee",
                FeeType.TRANSFER,
                BigDecimal.valueOf(1.00),
                BigDecimal.valueOf(0.50)
        ));

        assertEquals("TRN-FEE", response.feeCode());
        assertTrue(feeConfigRepository.existsByFeeCode("TRN-FEE"));
    }

    @Test
    void chargeFeePersistsCalculatedChargeInPostgres() {
        feeService.createConfig(new CreateFeeConfigRequest(
                "TRN-FEE",
                "Transfer Fee",
                FeeType.TRANSFER,
                BigDecimal.valueOf(1.00),
                BigDecimal.valueOf(0.50)
        ));

        FeeChargeResponse response = feeService.chargeFee(new ChargeFeeRequest(1L, "trn-fee", BigDecimal.valueOf(1000), "try"));

        assertEquals(new BigDecimal("6.00"), response.feeAmount());
        assertEquals(1, feeChargeRepository.findByAccountIdOrderByIdDesc(1L).size());
    }
}