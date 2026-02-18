package com.cbs.loan.dto;

import com.cbs.loan.model.LoanType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateLoanRequest(
        @NotNull Long customerId,
        @NotNull Long accountId,
        @NotBlank @Size(max = 32) String loanNumber,
        @NotNull LoanType loanType,
        @NotNull @DecimalMin("0.01") @Digits(integer = 17, fraction = 2) BigDecimal principalAmount,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2) BigDecimal annualInterestRate,
        @NotNull Integer termMonths,
        @NotNull LocalDate startDate,
        @NotNull LocalDate maturityDate
) {
}
