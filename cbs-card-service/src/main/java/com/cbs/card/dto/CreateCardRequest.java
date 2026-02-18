package com.cbs.card.dto;

import com.cbs.card.model.CardType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateCardRequest(
        @NotNull Long customerId,
        @NotNull Long accountId,
        @NotBlank @Size(max = 32) String cardNumber,
        @NotBlank @Size(max = 64) String token,
        @NotNull CardType cardType,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal dailyLimit,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal monthlyLimit,
        @NotNull LocalDate expiryDate
) {
}
