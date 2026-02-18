package com.cbs.interest.dto;

import com.cbs.interest.model.InterestAccrual;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InterestAccrualResponse(
        Long id,
        Long accountId,
        String productCode,
        BigDecimal principalAmount,
        BigDecimal accruedAmount,
        LocalDate accrualDate
) {
    public static InterestAccrualResponse from(InterestAccrual accrual) {
        return new InterestAccrualResponse(
                accrual.getId(),
                accrual.getAccountId(),
                accrual.getProductCode(),
                accrual.getPrincipalAmount(),
                accrual.getAccruedAmount(),
                accrual.getAccrualDate()
        );
    }
}
