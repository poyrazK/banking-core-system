package com.cbs.account.controller;

import com.cbs.account.dto.AccountResponse;
import com.cbs.account.dto.BalanceUpdateRequest;
import com.cbs.account.dto.CreateAccountRequest;
import com.cbs.account.dto.UpdateAccountStatusRequest;
import com.cbs.account.service.AccountService;
import com.cbs.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/accounts")
@Tag(name = "Accounts", description = "Endpoints for bank account management")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse accountResponse = accountService.createAccount(request);
        return ResponseEntity.ok(ApiResponse.success("Account created", accountResponse));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(@PathVariable("accountId") Long accountId) {
        AccountResponse accountResponse = accountService.getAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success("Account retrieved", accountResponse));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> listAccounts(
            @RequestParam(value = "customerId", required = false) Long customerId) {
        List<AccountResponse> accountResponses = accountService.listAccounts(customerId);
        return ResponseEntity.ok(ApiResponse.success("Accounts retrieved", accountResponses));
    }

    @PatchMapping("/{accountId}/credit")
    public ResponseEntity<ApiResponse<AccountResponse>> creditBalance(
            @PathVariable("accountId") Long accountId,
            @Valid @RequestBody BalanceUpdateRequest request) {
        AccountResponse accountResponse = accountService.creditBalance(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("Account credited", accountResponse));
    }

    @PatchMapping("/{accountId}/debit")
    public ResponseEntity<ApiResponse<AccountResponse>> debitBalance(
            @PathVariable("accountId") Long accountId,
            @Valid @RequestBody BalanceUpdateRequest request) {
        AccountResponse accountResponse = accountService.debitBalance(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("Account debited", accountResponse));
    }

    @PatchMapping("/{accountId}/status")
    public ResponseEntity<ApiResponse<AccountResponse>> updateStatus(
            @PathVariable("accountId") Long accountId,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        AccountResponse accountResponse = accountService.updateStatus(accountId, request);
        return ResponseEntity.ok(ApiResponse.success("Account status updated", accountResponse));
    }
}
