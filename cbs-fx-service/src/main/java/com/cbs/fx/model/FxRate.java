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
        name = "fx_rates",
        uniqueConstraints = @UniqueConstraint(name = "uk_fx_rates_pair", columnNames = "currency_pair")
)
public class FxRate extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "currency_pair", nullable = false, length = 7)
    private String currencyPair;

    @Column(name = "base_currency", nullable = false, length = 3)
    private String baseCurrency;

    @Column(name = "quote_currency", nullable = false, length = 3)
    private String quoteCurrency;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal midRate;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal buySpreadBps;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal sellSpreadBps;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FxRateStatus status = FxRateStatus.ACTIVE;

    public FxRate() {
    }

    public FxRate(String baseCurrency,
                  String quoteCurrency,
                  BigDecimal midRate,
                  BigDecimal buySpreadBps,
                  BigDecimal sellSpreadBps) {
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.currencyPair = baseCurrency + "/" + quoteCurrency;
        this.midRate = midRate;
        this.buySpreadBps = buySpreadBps;
        this.sellSpreadBps = sellSpreadBps;
    }

    public Long getId() {
        return id;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public String getQuoteCurrency() {
        return quoteCurrency;
    }

    public BigDecimal getMidRate() {
        return midRate;
    }

    public BigDecimal getBuySpreadBps() {
        return buySpreadBps;
    }

    public BigDecimal getSellSpreadBps() {
        return sellSpreadBps;
    }

    public FxRateStatus getStatus() {
        return status;
    }

    public void setMidRate(BigDecimal midRate) {
        this.midRate = midRate;
    }

    public void setBuySpreadBps(BigDecimal buySpreadBps) {
        this.buySpreadBps = buySpreadBps;
    }

    public void setSellSpreadBps(BigDecimal sellSpreadBps) {
        this.sellSpreadBps = sellSpreadBps;
    }

    public void setStatus(FxRateStatus status) {
        this.status = status;
    }
}
