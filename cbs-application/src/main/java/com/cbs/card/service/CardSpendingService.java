package com.cbs.card.service;

import com.cbs.card.dto.SpendingLimitResponse;
import com.cbs.card.model.Card;
import com.cbs.card.model.CardSpendingRecord;
import com.cbs.card.model.CardStatus;
import com.cbs.card.repository.CardRepository;
import com.cbs.card.repository.CardSpendingRepository;
import com.cbs.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CardSpendingService {

    private final CardRepository cardRepository;
    private final CardSpendingRepository cardSpendingRepository;

    public CardSpendingService(CardRepository cardRepository,
            CardSpendingRepository cardSpendingRepository) {
        this.cardRepository = cardRepository;
        this.cardSpendingRepository = cardSpendingRepository;
    }

    @Transactional
    public void validateAndRecordSpending(Long cardId, BigDecimal amount, String transactionReference) {
        Card card = findActiveCard(cardId);
        LocalDate today = LocalDate.now();

        BigDecimal dailySpent = cardSpendingRepository.sumDailySpending(cardId, today);
        if (dailySpent.add(amount).compareTo(card.getDailyLimit()) > 0) {
            throw new ApiException("CARD_DAILY_LIMIT_EXCEEDED",
                    "Daily spending limit exceeded. Limit: " + card.getDailyLimit()
                            + ", already spent: " + dailySpent + ", requested: " + amount);
        }

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        BigDecimal monthlySpent = cardSpendingRepository.sumMonthlySpending(cardId, startOfMonth, endOfMonth);
        if (monthlySpent.add(amount).compareTo(card.getMonthlyLimit()) > 0) {
            throw new ApiException("CARD_MONTHLY_LIMIT_EXCEEDED",
                    "Monthly spending limit exceeded. Limit: " + card.getMonthlyLimit()
                            + ", already spent: " + monthlySpent + ", requested: " + amount);
        }

        cardSpendingRepository.save(new CardSpendingRecord(cardId, amount, transactionReference, today));
    }

    @Transactional(readOnly = true)
    public SpendingLimitResponse getSpendingStatus(Long cardId) {
        Card card = findCard(cardId);
        LocalDate today = LocalDate.now();

        BigDecimal dailySpent = cardSpendingRepository.sumDailySpending(cardId, today);
        BigDecimal dailyRemaining = card.getDailyLimit().subtract(dailySpent).max(BigDecimal.ZERO);

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        BigDecimal monthlySpent = cardSpendingRepository.sumMonthlySpending(cardId, startOfMonth, endOfMonth);
        BigDecimal monthlyRemaining = card.getMonthlyLimit().subtract(monthlySpent).max(BigDecimal.ZERO);

        return new SpendingLimitResponse(
                cardId,
                card.getDailyLimit(),
                dailySpent,
                dailyRemaining,
                card.getMonthlyLimit(),
                monthlySpent,
                monthlyRemaining);
    }

    private Card findActiveCard(Long cardId) {
        Card card = findCard(cardId);
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new ApiException("CARD_NOT_ACTIVE",
                    "Card is not active. Current status: " + card.getStatus());
        }
        return card;
    }

    private Card findCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ApiException("CARD_NOT_FOUND", "Card not found"));
    }
}
