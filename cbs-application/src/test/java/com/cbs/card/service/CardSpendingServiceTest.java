package com.cbs.card.service;

import com.cbs.card.model.Card;
import com.cbs.card.model.CardStatus;
import com.cbs.card.model.CardType;
import com.cbs.card.dto.SpendingLimitResponse;
import com.cbs.card.repository.CardRepository;
import com.cbs.card.repository.CardSpendingRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardSpendingServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardSpendingRepository cardSpendingRepository;

    private CardSpendingService cardSpendingService;

    @BeforeEach
    void setUp() {
        cardSpendingService = new CardSpendingService(cardRepository, cardSpendingRepository);
    }

    private Card createActiveCard(BigDecimal dailyLimit, BigDecimal monthlyLimit) {
        Card card = new Card(1L, 100L, "4111111111111111", "TOKEN1",
                CardType.DEBIT, dailyLimit, monthlyLimit, LocalDate.of(2028, 12, 31));
        card.setStatus(CardStatus.ACTIVE);
        return card;
    }

    @Test
    void validateAndRecordSpending_allowsTransactionWithinDailyAndMonthlyLimits() {
        Card card = createActiveCard(new BigDecimal("1000.00"), new BigDecimal("5000.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardSpendingRepository.sumDailySpending(any(), any())).thenReturn(new BigDecimal("200.00"));
        when(cardSpendingRepository.sumMonthlySpending(any(), any(), any())).thenReturn(new BigDecimal("1000.00"));

        cardSpendingService.validateAndRecordSpending(1L, new BigDecimal("500.00"), "TX-001");

        verify(cardSpendingRepository).save(any());
    }

    @Test
    void validateAndRecordSpending_rejectsDailyLimitExceeded() {
        Card card = createActiveCard(new BigDecimal("1000.00"), new BigDecimal("5000.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardSpendingRepository.sumDailySpending(any(), any())).thenReturn(new BigDecimal("800.00"));

        ApiException exception = assertThrows(ApiException.class,
                () -> cardSpendingService.validateAndRecordSpending(
                        1L, new BigDecimal("300.00"), "TX-002"));

        assertEquals("CARD_DAILY_LIMIT_EXCEEDED", exception.getErrorCode());
    }

    @Test
    void validateAndRecordSpending_rejectsMonthlyLimitExceeded() {
        Card card = createActiveCard(new BigDecimal("5000.00"), new BigDecimal("3000.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardSpendingRepository.sumDailySpending(any(), any())).thenReturn(BigDecimal.ZERO);
        when(cardSpendingRepository.sumMonthlySpending(any(), any(), any())).thenReturn(new BigDecimal("2800.00"));

        ApiException exception = assertThrows(ApiException.class,
                () -> cardSpendingService.validateAndRecordSpending(
                        1L, new BigDecimal("500.00"), "TX-003"));

        assertEquals("CARD_MONTHLY_LIMIT_EXCEEDED", exception.getErrorCode());
    }

    @Test
    void validateAndRecordSpending_rejectsNonActiveCard() {
        Card card = createActiveCard(new BigDecimal("1000.00"), new BigDecimal("5000.00"));
        card.setStatus(CardStatus.FROZEN);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        ApiException exception = assertThrows(ApiException.class,
                () -> cardSpendingService.validateAndRecordSpending(
                        1L, new BigDecimal("100.00"), "TX-004"));

        assertEquals("CARD_NOT_ACTIVE", exception.getErrorCode());
    }

    @Test
    void validateAndRecordSpending_throwsWhenCardNotFound() {
        when(cardRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class,
                () -> cardSpendingService.validateAndRecordSpending(
                        99L, new BigDecimal("100.00"), "TX-005"));

        assertEquals("CARD_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void getSpendingStatus_returnsCorrectRemainingAmounts() {
        Card card = createActiveCard(new BigDecimal("1000.00"), new BigDecimal("5000.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardSpendingRepository.sumDailySpending(any(), any())).thenReturn(new BigDecimal("350.00"));
        when(cardSpendingRepository.sumMonthlySpending(any(), any(), any())).thenReturn(new BigDecimal("2200.00"));

        SpendingLimitResponse response = cardSpendingService.getSpendingStatus(1L);

        assertEquals(new BigDecimal("1000.00"), response.dailyLimit());
        assertEquals(new BigDecimal("350.00"), response.dailySpent());
        assertEquals(new BigDecimal("650.00"), response.dailyRemaining());
        assertEquals(new BigDecimal("5000.00"), response.monthlyLimit());
        assertEquals(new BigDecimal("2200.00"), response.monthlySpent());
        assertEquals(new BigDecimal("2800.00"), response.monthlyRemaining());
    }
}
