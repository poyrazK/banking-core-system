package com.cbs.interest.model;

import com.cbs.common.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "interest_accruals")
public class InterestAccrual extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 32)
    private String productCode;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal accruedAmount;

    @Column(nullable = false)
    private LocalDate accrualDate;

    public InterestAccrual() {
    }

    public InterestAccrual(Long accountId,
                           String productCode,
                           BigDecimal principalAmount,
                           BigDecimal accruedAmount,
                           LocalDate accrualDate) {
        this.accountId = accountId;
        this.productCode = productCode;
        this.principalAmount = principalAmount;
        this.accruedAmount = accruedAmount;
        this.accrualDate = accrualDate;
    }

    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getProductCode() {
        return productCode;
    }

    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public BigDecimal getAccruedAmount() {
        return accruedAmount;
    }

    public LocalDate getAccrualDate() {
        return accrualDate;
    }
}
