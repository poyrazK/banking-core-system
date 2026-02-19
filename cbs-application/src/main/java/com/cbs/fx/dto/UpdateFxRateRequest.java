package com.cbs.fx.dto;

import com.cbs.fx.model.FxRateStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateFxRateRequest(
        @NotNull @DecimalMin("0.00000001") @Digits(integer = 11, fraction = 8) BigDecimal midRate,
        @NotNull @DecimalMin("0.0000") @Digits(integer = 6, fraction = 4) BigDecimal buySpreadBps,
        @NotNull @DecimalMin("0.0000") @Digits(integer = 6, fraction = 4) BigDecimal sellSpreadBps,
        @NotNull FxRateStatus status
) {
}
