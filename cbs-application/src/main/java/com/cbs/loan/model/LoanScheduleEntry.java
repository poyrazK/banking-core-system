package com.cbs.loan.model;

import com.cbs.common.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_schedule_entries", indexes = @Index(name = "idx_loan_schedule_loan_id", columnList = "loanId"))
public class LoanScheduleEntry extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long loanId;

    @Column(nullable = false)
    private int installmentNumber;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal interestAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPayment;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal remainingBalance;

    @Column(nullable = false)
    private boolean paid = false;

    public LoanScheduleEntry() {
    }

    public LoanScheduleEntry(Long loanId,
            int installmentNumber,
            LocalDate dueDate,
            BigDecimal principalAmount,
            BigDecimal interestAmount,
            BigDecimal totalPayment,
            BigDecimal remainingBalance) {
        this.loanId = loanId;
        this.installmentNumber = installmentNumber;
        this.dueDate = dueDate;
        this.principalAmount = principalAmount;
        this.interestAmount = interestAmount;
        this.totalPayment = totalPayment;
        this.remainingBalance = remainingBalance;
    }

    public Long getId() {
        return id;
    }

    public Long getLoanId() {
        return loanId;
    }

    public int getInstallmentNumber() {
        return installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public BigDecimal getTotalPayment() {
        return totalPayment;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
