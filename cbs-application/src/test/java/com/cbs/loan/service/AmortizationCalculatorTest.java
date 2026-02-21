package com.cbs.loan.service;

import com.cbs.loan.model.AmortizationType;
import com.cbs.loan.model.LoanScheduleEntry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmortizationCalculatorTest {

    private final Long loanId = 1L;
    private final LocalDate startDate = LocalDate.of(2026, 1, 1);

    @Test
    void generateAnnuitySchedule_calculatesEqualPayments() {
        // Loan: 10,000 TL, 12% annual, 12 months
        BigDecimal principal = BigDecimal.valueOf(10000);
        BigDecimal rate = BigDecimal.valueOf(12);
        int term = 12;

        List<LoanScheduleEntry> schedule = AmortizationCalculator.generateSchedule(
                loanId, principal, rate, term, startDate, AmortizationType.ANNUITY);

        assertEquals(term, schedule.size());

        // Expected monthly payment for 10,000 @ 12% for 12m is ~888.49
        BigDecimal firstPayment = schedule.get(0).getTotalPayment();
        assertEquals(new BigDecimal("888.49"), firstPayment);

        // Check if all payments (except maybe last) are equal
        for (int i = 0; i < term - 1; i++) {
            assertEquals(firstPayment, schedule.get(i).getTotalPayment());
        }

        // Check if balance becomes zero
        assertEquals(BigDecimal.ZERO.setScale(2), schedule.get(term - 1).getRemainingBalance());

        // Total principal should be 10,000
        BigDecimal totalPrincipal = schedule.stream()
                .map(LoanScheduleEntry::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(principal.setScale(2), totalPrincipal);
    }

    @Test
    void generateFlatSchedule_calculatesSimpleInterest() {
        BigDecimal principal = BigDecimal.valueOf(10000);
        BigDecimal rate = BigDecimal.valueOf(12); // 12% per year = 1% per month
        int term = 12;

        List<LoanScheduleEntry> schedule = AmortizationCalculator.generateSchedule(
                loanId, principal, rate, term, startDate, AmortizationType.FLAT);

        // Interest = 10,000 * 0.12 * 1 = 1,200 TL
        // Monthly interest = 1,200 / 12 = 100 TL
        // Monthly principal = 10,000 / 12 = 833.33 TL
        // Monthly total = 933.33 TL

        assertEquals(new BigDecimal("100.00"), schedule.get(0).getInterestAmount());
        assertEquals(new BigDecimal("833.33"), schedule.get(0).getPrincipalAmount());
        assertEquals(new BigDecimal("933.33"), schedule.get(0).getTotalPayment());

        // Total interest should be 1,200
        BigDecimal totalInterest = schedule.stream()
                .map(LoanScheduleEntry::getInterestAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("1200.00"), totalInterest);

        // Sum principal amounts and assert equals original principal
        BigDecimal totalPrincipal = schedule.stream()
                .map(LoanScheduleEntry::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(principal.setScale(2), totalPrincipal);

        // Last remaining balance should be zero
        assertEquals(BigDecimal.ZERO.setScale(2), schedule.get(schedule.size() - 1).getRemainingBalance());
    }

    @Test
    void generateReducingBalanceSchedule_calculatesDecreasingInterest() {
        BigDecimal principal = BigDecimal.valueOf(10000);
        BigDecimal rate = BigDecimal.valueOf(12);
        int term = 10;

        List<LoanScheduleEntry> schedule = AmortizationCalculator.generateSchedule(
                loanId, principal, rate, term, startDate, AmortizationType.REDUCING_BALANCE);

        // Monthly principal = 10,000 / 10 = 1,000 TL
        assertEquals(new BigDecimal("1000.00"), schedule.get(0).getPrincipalAmount());

        // Month 1 interest: 10,000 * 0.01 = 100 TL
        assertEquals(new BigDecimal("100.00"), schedule.get(0).getInterestAmount());

        // Month 2 interest: 9,000 * 0.01 = 90 TL
        assertEquals(new BigDecimal("90.00"), schedule.get(1).getInterestAmount());

        assertTrue(schedule.get(1).getTotalPayment().compareTo(schedule.get(0).getTotalPayment()) < 0);

        // Sum principal amounts and assert equals original principal
        BigDecimal totalPrincipal = schedule.stream()
                .map(LoanScheduleEntry::getPrincipalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(principal.setScale(2), totalPrincipal);

        // Last remaining balance should be zero
        assertEquals(BigDecimal.ZERO.setScale(2), schedule.get(schedule.size() - 1).getRemainingBalance());
    }
}
