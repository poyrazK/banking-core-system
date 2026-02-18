package com.cbs.fx.model;

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
        name = "fx_deals",
        uniqueConstraints = @UniqueConstraint(name = "uk_fx_deals_reference", columnNames = "reference")
)
public class FxDeal extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long debitAccountId;

    @Column(nullable = false)
    private Long creditAccountId;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 3)
    private String quoteCurrency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private FxSide side;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal baseAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal quoteAmount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal rateApplied;

    @Column(nullable = false, length = 64)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FxDealStatus status = FxDealStatus.BOOKED;

    @Column(length = 255)
    private String cancelReason;

    public FxDeal() {
    }

    public FxDeal(Long customerId,
                  Long debitAccountId,
                  Long creditAccountId,
                  String baseCurrency,
                  String quoteCurrency,
                  FxSide side,
                  BigDecimal baseAmount,
                  BigDecimal quoteAmount,
                  BigDecimal rateApplied,
                  String reference) {
        this.customerId = customerId;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.side = side;
        this.baseAmount = baseAmount;
        this.quoteAmount = quoteAmount;
        this.rateApplied = rateApplied;
        this.reference = reference;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getDebitAccountId() {
        return debitAccountId;
    }

    public Long getCreditAccountId() {
        return creditAccountId;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public FxSide getSide() {
        return side;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public BigDecimal getQuoteAmount() {
        return quoteAmount;
    }

    public BigDecimal getRateApplied() {
        return rateApplied;
    }

    public String getReference() {
        return reference;
    }

    public FxDealStatus getStatus() {
        return status;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setStatus(FxDealStatus status) {
        this.status = status;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }
}
