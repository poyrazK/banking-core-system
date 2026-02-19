package com.cbs.deposit.model;

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
@Table(
        name = "deposit_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uk_deposit_accounts_number", columnNames = "deposit_number")
)
public class DepositAccount extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long settlementAccountId;

    @Column(name = "deposit_number", nullable = false, length = 32)
    private String depositNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DepositProductType productType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private DepositStatus status = DepositStatus.OPEN;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal annualInterestRate;

    @Column(nullable = false)
    private Integer termDays;

    @Column(nullable = false)
    private LocalDate openingDate;

    @Column(nullable = false)
    private LocalDate maturityDate;

    @Column(length = 255)
    private String statusReason;

    public DepositAccount() {
    }

    public DepositAccount(Long customerId,
                          Long settlementAccountId,
                          String depositNumber,
                          DepositProductType productType,
                          BigDecimal principalAmount,
                          BigDecimal annualInterestRate,
                          Integer termDays,
                          LocalDate openingDate,
                          LocalDate maturityDate) {
        this.customerId = customerId;
        this.settlementAccountId = settlementAccountId;
        this.depositNumber = depositNumber;
        this.productType = productType;
        this.principalAmount = principalAmount;
        this.currentAmount = principalAmount;
        this.annualInterestRate = annualInterestRate;
        this.termDays = termDays;
        this.openingDate = openingDate;
        this.maturityDate = maturityDate;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getSettlementAccountId() {
        return settlementAccountId;
    }

    public String getDepositNumber() {
        return depositNumber;
    }

    public DepositProductType getProductType() {
        return productType;
    }

    public DepositStatus getStatus() {
        return status;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public Integer getTermDays() {
        return termDays;
    }

    public LocalDate getOpeningDate() {
        return openingDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatus(DepositStatus status) {
        this.status = status;
    }

    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }
}
