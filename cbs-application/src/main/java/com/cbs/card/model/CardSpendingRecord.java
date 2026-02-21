package com.cbs.card.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "card_spending_records")
public class CardSpendingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long cardId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 64)
    private String transactionReference;

    @Column(nullable = false)
    private LocalDate spendingDate;

    public CardSpendingRecord() {
    }

    public CardSpendingRecord(Long cardId, BigDecimal amount, String transactionReference, LocalDate spendingDate) {
        this.cardId = cardId;
        this.amount = amount;
        this.transactionReference = transactionReference;
        this.spendingDate = spendingDate;
    }

    public Long getId() {
        return id;
    }

    public Long getCardId() {
        return cardId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public LocalDate getSpendingDate() {
        return spendingDate;
    }
}
