package com.cbs.fx.dto;

import com.cbs.fx.model.FxSide;

import java.math.BigDecimal;

public record FxQuoteResponse(
        String currencyPair,
        FxSide side,
        BigDecimal baseAmount,
        BigDecimal rate,
        BigDecimal quoteAmount
) {
}
