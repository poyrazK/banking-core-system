package com.cbs.card.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateCardLimitRequest(
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal dailyLimit,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal monthlyLimit
) {
}
