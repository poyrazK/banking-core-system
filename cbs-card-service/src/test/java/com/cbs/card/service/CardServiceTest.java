package com.cbs.card.service;

import com.cbs.card.dto.CardResponse;
import com.cbs.card.dto.CardStatusReasonRequest;
import com.cbs.card.dto.CreateCardRequest;
import com.cbs.card.dto.UpdateCardLimitRequest;
import com.cbs.card.model.Card;
import com.cbs.card.model.CardStatus;
import com.cbs.card.model.CardType;
import com.cbs.card.repository.CardRepository;
import com.cbs.common.exception.ApiException;
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
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    private CardService cardService;

    @BeforeEach
    void setUp() {
        cardService = new CardService(cardRepository);
    }

    @Test
    void createCard_normalizesCardNumberAndToken() {
        CreateCardRequest request = new CreateCardRequest(
                1L,
                10L,
                "  4111 1111 1111 1111  ",
                "  token-1  ",
                CardType.DEBIT,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(10000),
                LocalDate.now().plusYears(2)
        );
        when(cardRepository.existsByCardNumber("4111111111111111")).thenReturn(false);
        when(cardRepository.existsByToken("TOKEN-1")).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response = cardService.createCard(request);

        assertEquals("4111111111111111", response.cardNumber());
        assertEquals("TOKEN-1", response.token());
        assertEquals(CardStatus.NEW, response.status());
    }

    @Test
    void createCard_throwsWhenCardNumberExists() {
        CreateCardRequest request = new CreateCardRequest(
                1L,
                10L,
                "4111111111111111",
                "TOKEN-1",
                CardType.DEBIT,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(10000),
                LocalDate.now().plusYears(2)
        );
        when(cardRepository.existsByCardNumber("4111111111111111")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> cardService.createCard(request));

        assertEquals("CARD_NUMBER_EXISTS", exception.getErrorCode());
    }

    @Test
    void freezeCard_throwsWhenCardNotActive() {
        Card card = createCardWithStatus(CardStatus.NEW);
        when(cardRepository.findById(11L)).thenReturn(Optional.of(card));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> cardService.freezeCard(11L, new CardStatusReasonRequest("risk"))
        );

        assertEquals("CARD_NOT_ACTIVE", exception.getErrorCode());
    }

    @Test
    void updateLimits_throwsWhenCardBlocked() {
        Card card = createCardWithStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(12L)).thenReturn(Optional.of(card));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> cardService.updateLimits(12L, new UpdateCardLimitRequest(BigDecimal.valueOf(2000), BigDecimal.valueOf(20000)))
        );

        assertEquals("CARD_LIMIT_UPDATE_NOT_ALLOWED", exception.getErrorCode());
    }

    @Test
    void blockCard_setsStatusAndReason() {
        Card card = createCardWithStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(13L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardResponse response = cardService.blockCard(13L, new CardStatusReasonRequest("  suspicious  "));

        assertEquals(CardStatus.BLOCKED, response.status());
        assertEquals("suspicious", response.statusReason());
    }

    private Card createCardWithStatus(CardStatus status) {
        Card card = new Card(
                1L,
                10L,
                "4111111111111111",
                "TOKEN-1",
                CardType.DEBIT,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(10000),
                LocalDate.now().plusYears(2)
        );
        card.setStatus(status);
        return card;
    }
}
