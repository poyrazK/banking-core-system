package com.cbs.fee.dto;

import com.cbs.fee.model.FeeCharge;

import java.math.BigDecimal;

public record FeeChargeResponse(
        Long id,
        Long accountId,
        String feeCode,
        BigDecimal baseAmount,
        BigDecimal feeAmount,
        String currency
) {
    public static FeeChargeResponse from(FeeCharge charge) {
        return new FeeChargeResponse(
                charge.getId(),
                charge.getAccountId(),
                charge.getFeeCode(),
                charge.getBaseAmount(),
                charge.getFeeAmount(),
                charge.getCurrency()
        );
    }
}
