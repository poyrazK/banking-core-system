package com.cbs.deposit.dto;

import com.cbs.deposit.model.DepositAccount;
import com.cbs.deposit.model.DepositProductType;
import com.cbs.deposit.model.DepositStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DepositResponse(
        Long id,
        Long customerId,
        Long settlementAccountId,
        String depositNumber,
        DepositProductType productType,
        DepositStatus status,
        BigDecimal principalAmount,
        BigDecimal currentAmount,
        BigDecimal annualInterestRate,
        Integer termDays,
        LocalDate openingDate,
        LocalDate maturityDate,
        String statusReason
) {
    public static DepositResponse from(DepositAccount account) {
        return new DepositResponse(
                account.getId(),
                account.getCustomerId(),
                account.getSettlementAccountId(),
                account.getDepositNumber(),
                account.getProductType(),
                account.getStatus(),
                account.getPrincipalAmount(),
                account.getCurrentAmount(),
                account.getAnnualInterestRate(),
                account.getTermDays(),
                account.getOpeningDate(),
                account.getMaturityDate(),
                account.getStatusReason()
        );
    }
}
