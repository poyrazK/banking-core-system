package com.cbs.fx.dto;

import com.cbs.fx.model.FxRate;
import com.cbs.fx.model.FxRateStatus;

import java.math.BigDecimal;

public record FxRateResponse(
        Long id,
        String currencyPair,
        String baseCurrency,
        String quoteCurrency,
        BigDecimal midRate,
        BigDecimal buySpreadBps,
        BigDecimal sellSpreadBps,
        FxRateStatus status
) {
    public static FxRateResponse from(FxRate rate) {
        return new FxRateResponse(
                rate.getId(),
                rate.getCurrencyPair(),
                rate.getBaseCurrency(),
                rate.getQuoteCurrency(),
                rate.getMidRate(),
                rate.getBuySpreadBps(),
                rate.getSellSpreadBps(),
                rate.getStatus()
        );
    }
}
