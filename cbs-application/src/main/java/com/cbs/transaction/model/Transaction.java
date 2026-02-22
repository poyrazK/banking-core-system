package com.cbs.transaction.model;

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
@Table(name = "transactions", uniqueConstraints = @UniqueConstraint(name = "uk_transactions_reference", columnNames = "reference"))
public class Transaction extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long accountId;

    @Column
    private Long counterpartyAccountId;

    @Column
    private Long cardId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransactionStatus status = TransactionStatus.INITIATED;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false, length = 64)
    private String reference;

    @Column(nullable = false)
    private LocalDate valueDate;

    @Column(length = 255)
    private String reversalReason;

    @Column(length = 255)
    private String failureReason;

    @Column(name = "parent_transaction_id")
    private Long parentTransactionId;

    public Transaction() {
    }

    public Transaction(Long customerId,
            Long accountId,
            Long counterpartyAccountId,
            Long cardId,
            TransactionType type,
            BigDecimal amount,
            String currency,
            String description,
            String reference,
            LocalDate valueDate) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.counterpartyAccountId = counterpartyAccountId;
        this.cardId = cardId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.reference = reference;
        this.valueDate = valueDate;
    }

    public Transaction(Long customerId,
            Long accountId,
            Long counterpartyAccountId,
            Long cardId,
            TransactionType type,
            BigDecimal amount,
            String currency,
            String description,
            String reference,
            LocalDate valueDate,
            Long parentTransactionId) {
        this(customerId, accountId, counterpartyAccountId, cardId, type, amount, currency, description, reference, valueDate);
        this.parentTransactionId = parentTransactionId;
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

    public Long getCounterpartyAccountId() {
        return counterpartyAccountId;
    }

    public Long getCardId() {
        return cardId;
    }

    public TransactionType getType() {
        return type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public String getReference() {
        return reference;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public String getReversalReason() {
        return reversalReason;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public Long getParentTransactionId() {
        return parentTransactionId;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setReversalReason(String reversalReason) {
        this.reversalReason = reversalReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setParentTransactionId(Long parentTransactionId) {
        this.parentTransactionId = parentTransactionId;
    }
}
