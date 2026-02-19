package com.cbs.deposit.dto;

import com.cbs.deposit.model.DepositProductType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDepositRequest(
        @NotNull Long customerId,
        @NotNull Long settlementAccountId,
        @NotBlank @Size(max = 32) String depositNumber,
        @NotNull DepositProductType productType,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal principalAmount,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2) BigDecimal annualInterestRate,
        @NotNull Integer termDays,
        @NotNull LocalDate openingDate,
        @NotNull LocalDate maturityDate
) {
}
