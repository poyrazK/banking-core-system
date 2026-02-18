package com.cbs.interest.service;

import com.cbs.interest.dto.CreateInterestConfigRequest;
import com.cbs.interest.dto.InterestAccrualResponse;
import com.cbs.interest.dto.InterestConfigResponse;
import com.cbs.interest.dto.RunAccrualRequest;
import com.cbs.interest.model.InterestBasis;
import com.cbs.interest.model.InterestConfig;
import com.cbs.interest.repository.InterestAccrualRepository;
import com.cbs.interest.repository.InterestConfigRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class InterestServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55438/cbs_interest_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private InterestService interestService;

    @Autowired
    private InterestConfigRepository interestConfigRepository;

    @Autowired
    private InterestAccrualRepository interestAccrualRepository;

    @AfterEach
    void cleanUp() {
        interestAccrualRepository.deleteAll();
        interestConfigRepository.deleteAll();
    }

    @Test
    void createConfigPersistsNormalizedProductCodeInPostgres() {
        InterestConfigResponse response = interestService.createConfig(new CreateInterestConfigRequest(
                "  sav-01  ",
                BigDecimal.valueOf(12.50),
                InterestBasis.SIMPLE,
                30
        ));

        assertEquals("SAV-01", response.productCode());
        assertTrue(interestConfigRepository.existsByProductCode("SAV-01"));
    }

    @Test
    void runAccrualPersistsAccruedAmountInPostgres() {
        interestService.createConfig(new CreateInterestConfigRequest(
                "SAV-02",
                BigDecimal.valueOf(12),
                InterestBasis.SIMPLE,
                30
        ));

        InterestAccrualResponse response = interestService.runAccrual(
                new RunAccrualRequest(1L, "sav-02", BigDecimal.valueOf(10000), LocalDate.of(2026, 2, 18))
        );

        assertEquals("SAV-02", response.productCode());
        InterestConfig config = interestConfigRepository.findByProductCode("SAV-02").orElseThrow();
        assertEquals(1L, interestAccrualRepository.findByProductCodeOrderByIdDesc(config.getProductCode()).size());
    }
}