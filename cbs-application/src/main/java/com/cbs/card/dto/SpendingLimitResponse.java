package com.cbs.card.dto;

import java.math.BigDecimal;

public record SpendingLimitResponse(
        Long cardId,
        BigDecimal dailyLimit,
        BigDecimal dailySpent,
        BigDecimal dailyRemaining,
        BigDecimal monthlyLimit,
        BigDecimal monthlySpent,
        BigDecimal monthlyRemaining) {
}
