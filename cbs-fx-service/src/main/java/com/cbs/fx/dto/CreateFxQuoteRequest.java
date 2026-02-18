package com.cbs.fx.dto;

import com.cbs.fx.model.FxSide;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreateFxQuoteRequest(
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String baseCurrency,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String quoteCurrency,
        @NotNull FxSide side,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal baseAmount
) {
}
