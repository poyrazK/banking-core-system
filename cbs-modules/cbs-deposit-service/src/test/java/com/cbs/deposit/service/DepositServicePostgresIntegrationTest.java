package com.cbs.deposit.service;

import com.cbs.deposit.dto.AccrueInterestRequest;
import com.cbs.deposit.dto.CreateDepositRequest;
import com.cbs.deposit.dto.DepositResponse;
import com.cbs.deposit.dto.DepositStatusReasonRequest;
import com.cbs.deposit.model.DepositAccount;
import com.cbs.deposit.model.DepositProductType;
import com.cbs.deposit.model.DepositStatus;
import com.cbs.deposit.repository.DepositAccountRepository;
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
class DepositServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55437/cbs_deposit_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private DepositService depositService;

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @AfterEach
    void cleanUp() {
        depositAccountRepository.deleteAll();
    }

    @Test
    void createDepositPersistsNormalizedDepositNumberInPostgres() {
        DepositResponse response = depositService.createDeposit(new CreateDepositRequest(
                1L,
                10L,
                "  dep-001  ",
                DepositProductType.TERM,
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(18.0),
                90,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 5, 2)
        ));

        assertEquals("DEP-001", response.depositNumber());
        assertTrue(depositAccountRepository.existsByDepositNumber("DEP-001"));
    }

    @Test
    void accrueAndBreakPersistStateInPostgres() {
        DepositResponse created = depositService.createDeposit(new CreateDepositRequest(
                2L,
                20L,
                "DEP-002",
            DepositProductType.DEMAND,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(10),
                30,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 3, 3)
        ));

        depositService.accrueInterest(created.id(), new AccrueInterestRequest(BigDecimal.valueOf(50)));
        DepositResponse broken = depositService.breakDeposit(created.id(), new DepositStatusReasonRequest(" early exit "));

        assertEquals(DepositStatus.BROKEN, broken.status());
        assertEquals("early exit", broken.statusReason());
        DepositAccount persisted = depositAccountRepository.findById(created.id()).orElseThrow();
        assertEquals(0, persisted.getCurrentAmount().compareTo(new BigDecimal("1050")));
    }
}