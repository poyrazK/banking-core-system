package com.cbs.transaction.dto;

import com.cbs.transaction.model.Transaction;
import com.cbs.transaction.model.TransactionStatus;
import com.cbs.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        Long customerId,
        Long accountId,
        Long counterpartyAccountId,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        String currency,
        String description,
        String reference,
        LocalDate valueDate,
        String reversalReason,
        String failureReason
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getCustomerId(),
                transaction.getAccountId(),
                transaction.getCounterpartyAccountId(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getReference(),
                transaction.getValueDate(),
                transaction.getReversalReason(),
                transaction.getFailureReason()
        );
    }
}
