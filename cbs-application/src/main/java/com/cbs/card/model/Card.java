package com.cbs.card.model;

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
        name = "cards",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cards_card_number", columnNames = "card_number"),
                @UniqueConstraint(name = "uk_cards_token", columnNames = "token")
        }
)
public class Card extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long accountId;

    @Column(name = "card_number", nullable = false, length = 32)
    private String cardNumber;

    @Column(nullable = false, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private CardStatus status = CardStatus.NEW;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyLimit;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(length = 255)
    private String statusReason;

    public Card() {
    }

    public Card(Long customerId,
                Long accountId,
                String cardNumber,
                String token,
                CardType cardType,
                BigDecimal dailyLimit,
                BigDecimal monthlyLimit,
                LocalDate expiryDate) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.cardNumber = cardNumber;
        this.token = token;
        this.cardType = cardType;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
        this.expiryDate = expiryDate;
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

    public String getCardNumber() {
        return cardNumber;
    }

    public String getToken() {
        return token;
    }

    public CardType getCardType() {
        return cardType;
    }

    public CardStatus getStatus() {
        return status;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public void setDailyLimit(BigDecimal dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public void setMonthlyLimit(BigDecimal monthlyLimit) {
        this.monthlyLimit = monthlyLimit;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }
}
