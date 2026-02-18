package com.cbs.card.dto;

import com.cbs.card.model.Card;
import com.cbs.card.model.CardStatus;
import com.cbs.card.model.CardType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CardResponse(
        Long id,
        Long customerId,
        Long accountId,
        String cardNumber,
        String token,
        CardType cardType,
        CardStatus status,
        BigDecimal dailyLimit,
        BigDecimal monthlyLimit,
        LocalDate expiryDate,
        String statusReason
) {
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getCustomerId(),
                card.getAccountId(),
                card.getCardNumber(),
                card.getToken(),
                card.getCardType(),
                card.getStatus(),
                card.getDailyLimit(),
                card.getMonthlyLimit(),
                card.getExpiryDate(),
                card.getStatusReason()
        );
    }
}
