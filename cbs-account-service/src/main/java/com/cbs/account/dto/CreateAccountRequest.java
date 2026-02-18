package com.cbs.account.dto;

import com.cbs.account.model.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(
        @NotNull Long customerId,
        @NotBlank @Size(max = 32) String accountNumber,
        @NotNull AccountType accountType,
        @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal initialBalance
) {
}
