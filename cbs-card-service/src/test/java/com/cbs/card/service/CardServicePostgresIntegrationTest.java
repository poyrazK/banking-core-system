package com.cbs.card.service;

import com.cbs.card.dto.CardResponse;
import com.cbs.card.dto.CardStatusReasonRequest;
import com.cbs.card.dto.CreateCardRequest;
import com.cbs.card.model.Card;
import com.cbs.card.model.CardStatus;
import com.cbs.card.model.CardType;
import com.cbs.card.repository.CardRepository;
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
class CardServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55440/cbs_card_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @AfterEach
    void cleanUp() {
        cardRepository.deleteAll();
    }

    @Test
    void createCardPersistsNormalizedCardAndTokenInPostgres() {
        CardResponse response = cardService.createCard(new CreateCardRequest(
                1L,
                10L,
                "  4111 1111 1111 1111  ",
                "  token-1  ",
                CardType.DEBIT,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(10000),
                LocalDate.now().plusYears(2)
        ));

        assertEquals("4111111111111111", response.cardNumber());
        assertEquals("TOKEN-1", response.token());
        assertTrue(cardRepository.existsByCardNumber("4111111111111111"));
    }

    @Test
    void blockCardPersistsStatusAndReasonInPostgres() {
        CardResponse created = cardService.createCard(new CreateCardRequest(
                2L,
                20L,
                "4222222222222222",
                "TOKEN-2",
                CardType.CREDIT,
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(20000),
                LocalDate.now().plusYears(3)
        ));

        cardService.activateCard(created.id());
        CardResponse blocked = cardService.blockCard(created.id(), new CardStatusReasonRequest(" suspicious "));

        assertEquals(CardStatus.BLOCKED, blocked.status());
        assertEquals("suspicious", blocked.statusReason());
        Card persisted = cardRepository.findById(created.id()).orElseThrow();
        assertEquals(CardStatus.BLOCKED, persisted.getStatus());
    }
}