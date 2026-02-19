package com.cbs.account.dto;

import com.cbs.account.model.Currency;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BalanceUpdateRequest(
                @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal amount,
                Currency currency) {
        public BalanceUpdateRequest(BigDecimal amount) {
                this(amount, null);
        }
}
