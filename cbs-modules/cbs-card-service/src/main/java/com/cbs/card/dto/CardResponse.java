package com.cbs.card.dto;

import com.cbs.card.model.Card;
import com.cbs.card.model.CardStatus;
import com.cbs.card.model.CardType;
import com.cbs.common.security.CardNumberMasker;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object representing a sensitive card response with masked PAN.
 */
public record CardResponse(
        Long id,
        Long customerId,
        Long accountId,
        String maskedCardNumber,
        String token,
        CardType cardType,
        CardStatus status,
        BigDecimal dailyLimit,
        BigDecimal monthlyLimit,
        LocalDate expiryDate,
        String statusReason) {

    /**
     * Maps a Card entity to a CardResponse, applying PAN masking.
     */
    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getCustomerId(),
                card.getAccountId(),
                CardNumberMasker.mask(card.getCardNumber()),
                card.getToken(),
                card.getCardType(),
                card.getStatus(),
                card.getDailyLimit(),
                card.getMonthlyLimit(),
                card.getExpiryDate(),
                card.getStatusReason());
    }
}
