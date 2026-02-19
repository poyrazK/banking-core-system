package com.cbs.interest.dto;

import com.cbs.interest.model.InterestBasis;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateInterestConfigRequest(
        @NotBlank @Size(max = 32) String productCode,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2) BigDecimal annualRate,
        @NotNull InterestBasis interestBasis,
        @NotNull Integer accrualFrequencyDays
) {
}
