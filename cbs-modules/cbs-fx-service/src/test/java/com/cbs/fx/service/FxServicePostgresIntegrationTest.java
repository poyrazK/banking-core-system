package com.cbs.fx.service;

import com.cbs.fx.dto.BookFxDealRequest;
import com.cbs.fx.dto.CancelFxDealRequest;
import com.cbs.fx.dto.CreateFxRateRequest;
import com.cbs.fx.dto.FxDealResponse;
import com.cbs.fx.dto.FxRateResponse;
import com.cbs.fx.model.FxDeal;
import com.cbs.fx.model.FxDealStatus;
import com.cbs.fx.model.FxSide;
import com.cbs.fx.repository.FxDealRepository;
import com.cbs.fx.repository.FxRateRepository;
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
class FxServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55441/cbs_fx_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private FxService fxService;

    @Autowired
    private FxRateRepository fxRateRepository;

    @Autowired
    private FxDealRepository fxDealRepository;

    @AfterEach
    void cleanUp() {
        fxDealRepository.deleteAll();
        fxRateRepository.deleteAll();
    }

    @Test
    void createRatePersistsCurrencyPairInPostgres() {
        FxRateResponse response = fxService.createRate(new CreateFxRateRequest(
                "usd",
                "try",
                new BigDecimal("35.00000000"),
                new BigDecimal("10.0000"),
                new BigDecimal("12.0000")
        ));

        assertEquals("USD/TRY", response.currencyPair());
        assertTrue(fxRateRepository.existsByCurrencyPair("USD/TRY"));
    }

    @Test
    void bookAndCancelDealPersistStatusAndReason() {
        fxService.createRate(new CreateFxRateRequest(
                "USD",
                "TRY",
                new BigDecimal("35.00000000"),
                new BigDecimal("10.0000"),
                new BigDecimal("12.0000")
        ));

        FxDealResponse booked = fxService.bookDeal(new BookFxDealRequest(
                1L,
                10L,
                20L,
                "usd",
                "try",
                FxSide.BUY,
                new BigDecimal("100.00"),
                "ref-1"
        ));

        FxDealResponse cancelled = fxService.cancelDeal(booked.id(), new CancelFxDealRequest(" duplicate "));
        assertEquals(FxDealStatus.CANCELLED, cancelled.status());

        FxDeal persisted = fxDealRepository.findById(booked.id()).orElseThrow();
        assertEquals(FxDealStatus.CANCELLED, persisted.getStatus());
        assertEquals("duplicate", persisted.getCancelReason());
    }
}