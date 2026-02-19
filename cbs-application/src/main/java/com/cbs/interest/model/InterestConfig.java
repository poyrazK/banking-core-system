package com.cbs.interest.model;

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
        name = "interest_configs",
        uniqueConstraints = @UniqueConstraint(name = "uk_interest_configs_product_code", columnNames = "product_code")
)
public class InterestConfig extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false, length = 32)
    private String productCode;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal annualRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InterestBasis interestBasis;

    @Column(nullable = false)
    private Integer accrualFrequencyDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InterestStatus status = InterestStatus.ACTIVE;

    public InterestConfig() {
    }

    public InterestConfig(String productCode,
                          BigDecimal annualRate,
                          InterestBasis interestBasis,
                          Integer accrualFrequencyDays) {
        this.productCode = productCode;
        this.annualRate = annualRate;
        this.interestBasis = interestBasis;
        this.accrualFrequencyDays = accrualFrequencyDays;
    }

    public Long getId() {
        return id;
    }

    public String getProductCode() {
        return productCode;
    }

    public BigDecimal getAnnualRate() {
        return annualRate;
    }

    public InterestBasis getInterestBasis() {
        return interestBasis;
    }

    public Integer getAccrualFrequencyDays() {
        return accrualFrequencyDays;
    }

    public InterestStatus getStatus() {
        return status;
    }

    public void setAnnualRate(BigDecimal annualRate) {
        this.annualRate = annualRate;
    }

    public void setInterestBasis(InterestBasis interestBasis) {
        this.interestBasis = interestBasis;
    }

    public void setAccrualFrequencyDays(Integer accrualFrequencyDays) {
        this.accrualFrequencyDays = accrualFrequencyDays;
    }

    public void setStatus(InterestStatus status) {
        this.status = status;
    }
}
