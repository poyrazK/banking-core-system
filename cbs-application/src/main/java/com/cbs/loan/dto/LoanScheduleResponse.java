package com.cbs.loan.dto;

import com.cbs.loan.model.AmortizationType;
import com.cbs.loan.model.Loan;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record LoanScheduleResponse(
        Long loanId,
        String loanNumber,
        BigDecimal principalAmount,
        BigDecimal annualInterestRate,
        Integer termMonths,
        AmortizationType amortizationType,
        BigDecimal totalInterest,
        BigDecimal totalPayment,
        List<InstallmentEntry> schedule) {
    public record InstallmentEntry(
            int installmentNumber,
            LocalDate dueDate,
            BigDecimal principalAmount,
            BigDecimal interestAmount,
            BigDecimal totalPayment,
            BigDecimal remainingBalance,
            boolean paid) {
    }

    public static LoanScheduleResponse from(Loan loan, AmortizationType type,
            List<com.cbs.loan.model.LoanScheduleEntry> entries) {
        BigDecimal totalInterest = entries.stream()
                .map(com.cbs.loan.model.LoanScheduleEntry::getInterestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPayment = entries.stream()
                .map(com.cbs.loan.model.LoanScheduleEntry::getTotalPayment)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<InstallmentEntry> installments = entries.stream()
                .map(e -> new InstallmentEntry(
                        e.getInstallmentNumber(),
                        e.getDueDate(),
                        e.getPrincipalAmount(),
                        e.getInterestAmount(),
                        e.getTotalPayment(),
                        e.getRemainingBalance(),
                        e.isPaid()))
                .toList();

        return new LoanScheduleResponse(
                loan.getId(),
                loan.getLoanNumber(),
                loan.getPrincipalAmount(),
                loan.getAnnualInterestRate(),
                loan.getTermMonths(),
                type,
                totalInterest,
                totalPayment,
                installments);
    }
}
