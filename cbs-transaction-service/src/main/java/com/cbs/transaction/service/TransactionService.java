package com.cbs.transaction.service;

import com.cbs.common.exception.ApiException;
import com.cbs.transaction.dto.CreateTransactionRequest;
import com.cbs.transaction.dto.ReverseTransactionRequest;
import com.cbs.transaction.dto.TransactionResponse;
import com.cbs.transaction.integration.LedgerPostingClient;
import com.cbs.transaction.model.Transaction;
import com.cbs.transaction.model.TransactionStatus;
import com.cbs.transaction.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerPostingClient ledgerPostingClient;

    public TransactionService(TransactionRepository transactionRepository,
                              LedgerPostingClient ledgerPostingClient) {
        this.transactionRepository = transactionRepository;
        this.ledgerPostingClient = ledgerPostingClient;
    }

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        String reference = normalizeReference(request.reference());
        if (transactionRepository.existsByReference(reference)) {
            throw new ApiException("TRANSACTION_REFERENCE_EXISTS", "Reference already exists");
        }

        Transaction transaction = new Transaction(
                request.customerId(),
                request.accountId(),
                request.counterpartyAccountId(),
                request.type(),
                request.amount(),
                request.currency().trim().toUpperCase(),
                request.description().trim(),
                reference,
                request.valueDate()
        );

        Transaction createdTransaction = transactionRepository.save(transaction);
        createdTransaction.setStatus(TransactionStatus.PROCESSING);
        Transaction processingTransaction = transactionRepository.save(createdTransaction);

        try {
            ledgerPostingClient.postTransaction(processingTransaction);
            processingTransaction.setStatus(TransactionStatus.POSTED);
            processingTransaction.setFailureReason(null);
        } catch (ApiException exception) {
            processingTransaction.setStatus(TransactionStatus.FAILED);
            processingTransaction.setFailureReason(truncateReason(exception.getMessage()));
        }

        return TransactionResponse.from(transactionRepository.save(processingTransaction));
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransaction(Long transactionId) {
        return TransactionResponse.from(findTransaction(transactionId));
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> listTransactions(Long accountId, Long customerId) {
        List<Transaction> transactions;

        if (accountId != null && customerId != null) {
            transactions = transactionRepository.findByAccountIdAndCustomerIdOrderByIdDesc(accountId, customerId);
        } else if (accountId != null) {
            transactions = transactionRepository.findByAccountIdOrderByIdDesc(accountId);
        } else if (customerId != null) {
            transactions = transactionRepository.findByCustomerIdOrderByIdDesc(customerId);
        } else {
            transactions = transactionRepository.findAll().stream()
                    .sorted((left, right) -> right.getId().compareTo(left.getId()))
                    .toList();
        }

        return transactions.stream().map(TransactionResponse::from).toList();
    }

    @Transactional
    public TransactionResponse reverseTransaction(Long transactionId, ReverseTransactionRequest request) {
        Transaction transaction = findTransaction(transactionId);

        if (transaction.getStatus() == TransactionStatus.INITIATED || transaction.getStatus() == TransactionStatus.PROCESSING) {
            throw new ApiException("TRANSACTION_NOT_POSTED", "Only posted transaction can be reversed");
        }

        if (transaction.getStatus() == TransactionStatus.REVERSED) {
            throw new ApiException("TRANSACTION_ALREADY_REVERSED", "Transaction is already reversed");
        }

        if (transaction.getStatus() == TransactionStatus.FAILED) {
            throw new ApiException("TRANSACTION_FAILED", "Failed transaction cannot be reversed");
        }

        transaction.setStatus(TransactionStatus.REVERSED);
        transaction.setReversalReason(request.reason().trim());
        transaction.setFailureReason(null);

        return TransactionResponse.from(transactionRepository.save(transaction));
    }

    @Transactional
    public TransactionResponse retryPosting(Long transactionId) {
        Transaction transaction = findTransaction(transactionId);

        if (transaction.getStatus() != TransactionStatus.FAILED) {
            throw new ApiException("TRANSACTION_NOT_FAILED", "Only failed transaction can be retried");
        }

        transaction.setStatus(TransactionStatus.PROCESSING);
        transaction.setFailureReason(null);
        Transaction processingTransaction = transactionRepository.save(transaction);

        try {
            ledgerPostingClient.postTransaction(processingTransaction);
            processingTransaction.setStatus(TransactionStatus.POSTED);
            processingTransaction.setFailureReason(null);
        } catch (ApiException exception) {
            processingTransaction.setStatus(TransactionStatus.FAILED);
            processingTransaction.setFailureReason(truncateReason(exception.getMessage()));
        }

        return TransactionResponse.from(transactionRepository.save(processingTransaction));
    }

    private Transaction findTransaction(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ApiException("TRANSACTION_NOT_FOUND", "Transaction not found"));
    }

    private String normalizeReference(String reference) {
        return reference.trim().toUpperCase();
    }

    private String truncateReason(String reason) {
        if (reason == null) {
            return null;
        }
        return reason.length() <= 255 ? reason : reason.substring(0, 255);
    }
}
