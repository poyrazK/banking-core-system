package com.cbs.interest.dto;

import com.cbs.interest.model.InterestBasis;
import com.cbs.interest.model.InterestStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateInterestConfigRequest(
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2) BigDecimal annualRate,
        @NotNull InterestBasis interestBasis,
        @NotNull Integer accrualFrequencyDays,
        @NotNull InterestStatus status
) {
}
