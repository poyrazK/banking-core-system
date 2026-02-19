package com.cbs.payment.model;

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
        name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payments_reference", columnNames = "reference")
)
public class Payment extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long sourceAccountId;

    @Column
    private Long destinationAccountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency = "TRY";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status = PaymentStatus.INITIATED;

    @Column(nullable = false, length = 64)
    private String reference;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private LocalDate valueDate;

    @Column(length = 255)
    private String failureReason;

    public Payment() {
    }

    public Payment(Long customerId,
                   Long sourceAccountId,
                   Long destinationAccountId,
                   BigDecimal amount,
                   String currency,
                   PaymentMethod method,
                   String reference,
                   String description,
                   LocalDate valueDate) {
        this.customerId = customerId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.currency = currency;
        this.method = method;
        this.reference = reference;
        this.description = description;
        this.valueDate = valueDate;
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public Long getDestinationAccountId() {
        return destinationAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getReference() {
        return reference;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getValueDate() {
        return valueDate;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
