package com.cbs.fee.dto;

import com.cbs.fee.model.FeeConfig;
import com.cbs.fee.model.FeeStatus;
import com.cbs.fee.model.FeeType;

import java.math.BigDecimal;

public record FeeConfigResponse(
        Long id,
        String feeCode,
        String name,
        FeeType feeType,
        BigDecimal fixedAmount,
        BigDecimal percentageRate,
        FeeStatus status
) {
    public static FeeConfigResponse from(FeeConfig config) {
        return new FeeConfigResponse(
                config.getId(),
                config.getFeeCode(),
                config.getName(),
                config.getFeeType(),
                config.getFixedAmount(),
                config.getPercentageRate(),
                config.getStatus()
        );
    }
}
