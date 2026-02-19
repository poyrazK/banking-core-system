package com.cbs.transaction.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.cbs.common.api.ApiResponse;
import com.cbs.transaction.dto.CreateTransactionRequest;
import com.cbs.transaction.dto.ReverseTransactionRequest;
import com.cbs.transaction.dto.TransactionResponse;
import com.cbs.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@Tag(name = "Transactions", description = "Endpoints for bank transaction history and details")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return ResponseEntity.ok(ApiResponse.success("Transaction created", response));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<ApiResponse<TransactionResponse>> getTransaction(
            @PathVariable("transactionId") Long transactionId) {
        TransactionResponse response = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Transaction retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> listTransactions(
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "customerId", required = false) Long customerId) {
        List<TransactionResponse> responses = transactionService.listTransactions(accountId, customerId);
        return ResponseEntity.ok(ApiResponse.success("Transactions retrieved", responses));
    }

    @PatchMapping("/{transactionId}/reverse")
    public ResponseEntity<ApiResponse<TransactionResponse>> reverseTransaction(
            @PathVariable("transactionId") Long transactionId,
            @Valid @RequestBody ReverseTransactionRequest request) {
        TransactionResponse response = transactionService.reverseTransaction(transactionId, request);
        return ResponseEntity.ok(ApiResponse.success("Transaction reversed", response));
    }

    @PatchMapping("/{transactionId}/retry-posting")
    public ResponseEntity<ApiResponse<TransactionResponse>> retryPosting(
            @PathVariable("transactionId") Long transactionId) {
        TransactionResponse response = transactionService.retryPosting(transactionId);
        return ResponseEntity.ok(ApiResponse.success("Transaction posting retried", response));
    }
}
