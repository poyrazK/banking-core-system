package com.cbs.loan.model;

import com.cbs.common.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loans", uniqueConstraints = @UniqueConstraint(name = "uk_loans_loan_number", columnNames = "loan_number"))
public class Loan extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long accountId;

    @Column(name = "loan_number", nullable = false, length = 32)
    private String loanNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LoanStatus status = LoanStatus.APPLIED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AmortizationType amortizationType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal outstandingAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;

    @Column(nullable = false)
    private Integer termMonths;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate maturityDate;

    @Column(length = 255)
    private String decisionReason;

    public Loan() {
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    public Loan(Long customerId,
            Long accountId,
            String loanNumber,
            LoanType loanType,
            BigDecimal principalAmount,
            BigDecimal annualInterestRate,
            Integer termMonths,
            LocalDate startDate,
            LocalDate maturityDate,
            AmortizationType amortizationType) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.loanNumber = loanNumber;
        this.loanType = loanType;
        this.principalAmount = principalAmount;
        this.annualInterestRate = annualInterestRate;
        this.termMonths = termMonths;
        this.startDate = startDate;
        this.maturityDate = maturityDate;
        this.amortizationType = amortizationType;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public LoanType getLoanType() {
        return loanType;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public AmortizationType getAmortizationType() {
        return amortizationType;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public void setOutstandingAmount(BigDecimal outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }
}
