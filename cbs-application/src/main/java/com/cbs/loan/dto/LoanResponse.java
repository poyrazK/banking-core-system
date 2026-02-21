package com.cbs.loan.dto;

import com.cbs.loan.model.AmortizationType;
import com.cbs.loan.model.Loan;
import com.cbs.loan.model.LoanStatus;
import com.cbs.loan.model.LoanType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long customerId,
        Long accountId,
        String loanNumber,
        LoanType loanType,
        LoanStatus status,
        BigDecimal principalAmount,
        BigDecimal outstandingAmount,
        BigDecimal annualInterestRate,
        Integer termMonths,
        LocalDate startDate,
        LocalDate maturityDate,
        String decisionReason,
        AmortizationType amortizationType) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getCustomerId(),
                loan.getAccountId(),
                loan.getLoanNumber(),
                loan.getLoanType(),
                loan.getStatus(),
                loan.getPrincipalAmount(),
                loan.getOutstandingAmount(),
                loan.getAnnualInterestRate(),
                loan.getTermMonths(),
                loan.getStartDate(),
                loan.getMaturityDate(),
                loan.getDecisionReason(),
                loan.getAmortizationType());
    }
}
