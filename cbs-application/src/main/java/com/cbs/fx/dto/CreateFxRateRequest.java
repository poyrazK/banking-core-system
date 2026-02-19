package com.cbs.fx.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreateFxRateRequest(
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String baseCurrency,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String quoteCurrency,
        @NotNull @DecimalMin("0.00000001") @Digits(integer = 11, fraction = 8) BigDecimal midRate,
        @NotNull @DecimalMin("0.0000") @Digits(integer = 6, fraction = 4) BigDecimal buySpreadBps,
        @NotNull @DecimalMin("0.0000") @Digits(integer = 6, fraction = 4) BigDecimal sellSpreadBps
) {
}
