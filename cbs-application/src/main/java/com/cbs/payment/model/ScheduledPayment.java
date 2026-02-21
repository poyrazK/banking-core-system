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
@Table(name = "scheduled_payments", uniqueConstraints = @UniqueConstraint(name = "uk_scheduled_payments_reference", columnNames = "reference"))
public class ScheduledPayment extends AuditableEntity {

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
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentMethod method;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ScheduleFrequency frequency;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDate nextExecutionDate;

    @Column
    private LocalDate lastExecutedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ScheduledPaymentStatus status = ScheduledPaymentStatus.ACTIVE;

    @Column(nullable = false)
    private int executionCount = 0;

    @Column(nullable = false)
    private int failureCount = 0;

    @Column(length = 255)
    private String lastFailureReason;

    @Column(nullable = false, length = 64)
    private String reference;

    public ScheduledPayment() {
    }

    private ScheduledPayment(Builder builder) {
        this.customerId = builder.customerId;
        this.sourceAccountId = builder.sourceAccountId;
        this.destinationAccountId = builder.destinationAccountId;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.method = builder.method;
        this.description = builder.description;
        this.frequency = builder.frequency;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.reference = builder.reference;
        this.nextExecutionDate = builder.startDate;
    }

    public static class Builder {
        private Long customerId;
        private Long sourceAccountId;
        private Long destinationAccountId;
        private BigDecimal amount;
        private String currency;
        private PaymentMethod method;
        private String description;
        private ScheduleFrequency frequency;
        private LocalDate startDate;
        private LocalDate endDate;
        private String reference;

        public Builder customerId(Long customerId) { this.customerId = customerId; return this; }
        public Builder sourceAccountId(Long sourceAccountId) { this.sourceAccountId = sourceAccountId; return this; }
        public Builder destinationAccountId(Long destinationAccountId) { this.destinationAccountId = destinationAccountId; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder currency(String currency) { this.currency = currency; return this; }
        public Builder method(PaymentMethod method) { this.method = method; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder frequency(ScheduleFrequency frequency) { this.frequency = frequency; return this; }
        public Builder startDate(LocalDate startDate) { this.startDate = startDate; return this; }
        public Builder endDate(LocalDate endDate) { this.endDate = endDate; return this; }
        public Builder reference(String reference) { this.reference = reference; return this; }

        public ScheduledPayment build() {
            return new ScheduledPayment(this);
        }
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

    public String getDescription() {
        return description;
    }

    public ScheduleFrequency getFrequency() {
        return frequency;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public LocalDate getNextExecutionDate() {
        return nextExecutionDate;
    }

    public void setNextExecutionDate(LocalDate nextExecutionDate) {
        this.nextExecutionDate = nextExecutionDate;
    }

    public LocalDate getLastExecutedDate() {
        return lastExecutedDate;
    }

    public void setLastExecutedDate(LocalDate lastExecutedDate) {
        this.lastExecutedDate = lastExecutedDate;
    }

    public ScheduledPaymentStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduledPaymentStatus status) {
        this.status = status;
    }

    public int getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(int executionCount) {
        this.executionCount = executionCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }

    public void setLastFailureReason(String lastFailureReason) {
        this.lastFailureReason = lastFailureReason;
    }

    public String getReference() {
        return reference;
    }
}
