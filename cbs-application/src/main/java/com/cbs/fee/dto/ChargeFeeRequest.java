package com.cbs.fee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record ChargeFeeRequest(
        @NotNull Long accountId,
        @NotBlank String feeCode,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal baseAmount,
        @NotBlank @Pattern(regexp = "^[A-Z]{3}$") String currency
) {
}
