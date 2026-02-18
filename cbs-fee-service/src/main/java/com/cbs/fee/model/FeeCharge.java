package com.cbs.fee.model;

import com.cbs.common.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "fee_charges")
public class FeeCharge extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false, length = 32)
    private String feeCode;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal baseAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal feeAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    public FeeCharge() {
    }

    public FeeCharge(Long accountId,
                     String feeCode,
                     BigDecimal baseAmount,
                     BigDecimal feeAmount,
                     String currency) {
        this.accountId = accountId;
        this.feeCode = feeCode;
        this.baseAmount = baseAmount;
        this.feeAmount = feeAmount;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public String getFeeCode() {
        return feeCode;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public String getCurrency() {
        return currency;
    }
}
