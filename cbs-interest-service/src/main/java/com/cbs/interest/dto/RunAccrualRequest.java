package com.cbs.interest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RunAccrualRequest(
        @NotNull Long accountId,
        @NotNull String productCode,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal principalAmount,
        @NotNull LocalDate accrualDate
) {
}
