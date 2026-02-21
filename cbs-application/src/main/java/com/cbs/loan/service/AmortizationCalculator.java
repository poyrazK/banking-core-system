package com.cbs.loan.service;

import com.cbs.loan.model.AmortizationType;
import com.cbs.loan.model.LoanScheduleEntry;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AmortizationCalculator {

    private AmortizationCalculator() {
        // Utility class
    }

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public static List<LoanScheduleEntry> generateSchedule(
            Long loanId,
            BigDecimal principal,
            BigDecimal annualRate,
            int termMonths,
            LocalDate startDate,
            AmortizationType type) {

        if (termMonths <= 0) {
            throw new IllegalArgumentException("termMonths must be greater than 0");
        }
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("principal must be non-null and greater than 0");
        }

        return switch (type) {
            case ANNUITY -> generateAnnuitySchedule(loanId, principal, annualRate, termMonths, startDate);
            case FLAT -> generateFlatSchedule(loanId, principal, annualRate, termMonths, startDate);
            case REDUCING_BALANCE ->
                generateReducingBalanceSchedule(loanId, principal, annualRate, termMonths, startDate);
        };
    }

    private static List<LoanScheduleEntry> generateAnnuitySchedule(
            Long loanId, BigDecimal principal, BigDecimal annualRate, int termMonths, LocalDate startDate) {

        List<LoanScheduleEntry> schedule = new ArrayList<>();
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, ROUNDING_MODE);

        BigDecimal monthlyPayment;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            monthlyPayment = principal.divide(BigDecimal.valueOf(termMonths), SCALE, ROUNDING_MODE);
        } else {
            MathContext mc = new MathContext(15, ROUNDING_MODE);
            // PMT = P * [r(1+r)^n] / [(1+r)^n – 1]
            BigDecimal ratePlusOnePowN = monthlyRate.add(BigDecimal.ONE).pow(termMonths, mc);
            BigDecimal numerator = principal.multiply(monthlyRate).multiply(ratePlusOnePowN);
            BigDecimal denominator = ratePlusOnePowN.subtract(BigDecimal.ONE);
            monthlyPayment = numerator.divide(denominator, SCALE, ROUNDING_MODE);
        }

        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= termMonths; i++) {
            BigDecimal interest = remainingBalance.multiply(monthlyRate).setScale(SCALE, ROUNDING_MODE);

            // Guard interest against negative values
            if (interest.compareTo(BigDecimal.ZERO) < 0) {
                interest = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
            }

            BigDecimal principalPaid = monthlyPayment.subtract(interest);
            BigDecimal paymentForPeriod = monthlyPayment;

            // Adjust last installment for rounding remainders
            if (i == termMonths) {
                principalPaid = remainingBalance;
                paymentForPeriod = principalPaid.add(interest);
            }

            remainingBalance = remainingBalance.subtract(principalPaid);

            schedule.add(new LoanScheduleEntry(
                    loanId, i, startDate.plusMonths(i), principalPaid, interest, paymentForPeriod, remainingBalance));
        }

        return schedule;
    }

    private static List<LoanScheduleEntry> generateFlatSchedule(
            Long loanId, BigDecimal principal, BigDecimal annualRate, int termMonths, LocalDate startDate) {

        List<LoanScheduleEntry> schedule = new ArrayList<>();
        BigDecimal totalInterest = principal.multiply(annualRate)
                .multiply(BigDecimal.valueOf(termMonths))
                .divide(BigDecimal.valueOf(1200), SCALE, ROUNDING_MODE);

        BigDecimal monthlyInterest = totalInterest.divide(BigDecimal.valueOf(termMonths), SCALE, ROUNDING_MODE);
        BigDecimal monthlyPrincipal = principal.divide(BigDecimal.valueOf(termMonths), SCALE, ROUNDING_MODE);

        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= termMonths; i++) {
            BigDecimal p = monthlyPrincipal;
            BigDecimal interest = monthlyInterest;

            if (i == termMonths) {
                p = remainingBalance;
                interest = totalInterest.subtract(monthlyInterest.multiply(BigDecimal.valueOf((long) termMonths - 1)));
            }

            if (interest.compareTo(BigDecimal.ZERO) < 0) {
                interest = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
            }

            remainingBalance = remainingBalance.subtract(p);

            schedule.add(new LoanScheduleEntry(
                    loanId, i, startDate.plusMonths(i), p, interest, p.add(interest), remainingBalance));
        }

        return schedule;
    }

    private static List<LoanScheduleEntry> generateReducingBalanceSchedule(
            Long loanId, BigDecimal principal, BigDecimal annualRate, int termMonths, LocalDate startDate) {

        List<LoanScheduleEntry> schedule = new ArrayList<>();
        BigDecimal monthlyPrincipal = principal.divide(BigDecimal.valueOf(termMonths), SCALE, ROUNDING_MODE);
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, ROUNDING_MODE);

        BigDecimal remainingBalance = principal;

        for (int i = 1; i <= termMonths; i++) {
            BigDecimal p = monthlyPrincipal;
            if (i == termMonths) {
                p = remainingBalance;
            }

            BigDecimal interest = remainingBalance.multiply(monthlyRate).setScale(SCALE, ROUNDING_MODE);
            if (interest.compareTo(BigDecimal.ZERO) < 0) {
                interest = BigDecimal.ZERO.setScale(SCALE, ROUNDING_MODE);
            }

            remainingBalance = remainingBalance.subtract(p);

            schedule.add(new LoanScheduleEntry(
                    loanId, i, startDate.plusMonths(i), p, interest, p.add(interest), remainingBalance));
        }

        return schedule;
    }
}
