package com.cbs.fx.dto;

import com.cbs.fx.model.FxDeal;
import com.cbs.fx.model.FxDealStatus;
import com.cbs.fx.model.FxSide;

import java.math.BigDecimal;

public record FxDealResponse(
        Long id,
        Long customerId,
        Long debitAccountId,
        Long creditAccountId,
        String baseCurrency,
        String quoteCurrency,
        FxSide side,
        BigDecimal baseAmount,
        BigDecimal quoteAmount,
        BigDecimal rateApplied,
        String reference,
        FxDealStatus status,
        String cancelReason
) {
    public static FxDealResponse from(FxDeal deal) {
        return new FxDealResponse(
                deal.getId(),
                deal.getCustomerId(),
                deal.getDebitAccountId(),
                deal.getCreditAccountId(),
                deal.getBaseCurrency(),
                deal.getQuoteCurrency(),
                deal.getSide(),
                deal.getBaseAmount(),
                deal.getQuoteAmount(),
                deal.getRateApplied(),
                deal.getReference(),
                deal.getStatus(),
                deal.getCancelReason()
        );
    }
}
