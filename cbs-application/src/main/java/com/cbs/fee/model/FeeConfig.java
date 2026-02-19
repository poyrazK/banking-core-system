package com.cbs.fee.model;

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

@Entity
@Table(
        name = "fee_configs",
        uniqueConstraints = @UniqueConstraint(name = "uk_fee_configs_code", columnNames = "fee_code")
)
public class FeeConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fee_code", nullable = false, length = 32)
    private String feeCode;

    @Column(nullable = false, length = 128)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private FeeType feeType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fixedAmount;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentageRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FeeStatus status = FeeStatus.ACTIVE;

    public FeeConfig() {
    }

    public FeeConfig(String feeCode,
                     String name,
                     FeeType feeType,
                     BigDecimal fixedAmount,
                     BigDecimal percentageRate) {
        this.feeCode = feeCode;
        this.name = name;
        this.feeType = feeType;
        this.fixedAmount = fixedAmount;
        this.percentageRate = percentageRate;
    }

    public Long getId() {
        return id;
    }

    public String getFeeCode() {
        return feeCode;
    }

    public String getName() {
        return name;
    }

    public FeeType getFeeType() {
        return feeType;
    }

    public BigDecimal getFixedAmount() {
        return fixedAmount;
    }

    public BigDecimal getPercentageRate() {
        return percentageRate;
    }

    public FeeStatus getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFeeType(FeeType feeType) {
        this.feeType = feeType;
    }

    public void setFixedAmount(BigDecimal fixedAmount) {
        this.fixedAmount = fixedAmount;
    }

    public void setPercentageRate(BigDecimal percentageRate) {
        this.percentageRate = percentageRate;
    }

    public void setStatus(FeeStatus status) {
        this.status = status;
    }
}
