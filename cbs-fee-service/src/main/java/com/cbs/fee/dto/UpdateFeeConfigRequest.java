package com.cbs.fee.dto;

import com.cbs.fee.model.FeeStatus;
import com.cbs.fee.model.FeeType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateFeeConfigRequest(
        @NotBlank @Size(max = 128) String name,
        @NotNull FeeType feeType,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal fixedAmount,
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2) BigDecimal percentageRate,
        @NotNull FeeStatus status
) {
}
