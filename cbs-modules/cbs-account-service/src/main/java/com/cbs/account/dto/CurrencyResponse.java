package com.cbs.account.dto;

import com.cbs.account.model.Currency;

public record CurrencyResponse(Long accountId, Currency currency) {
}
