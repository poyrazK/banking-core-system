package com.cbs.ledger.controller;

import com.cbs.common.api.ApiResponse;
import com.cbs.ledger.dto.AccountResponse;
import com.cbs.ledger.dto.BalanceResponse;
import com.cbs.ledger.dto.CreateAccountRequest;
import com.cbs.ledger.dto.PostJournalEntryRequest;
import com.cbs.ledger.dto.PostJournalEntryResponse;
import com.cbs.ledger.dto.ReconciliationResponse;
import com.cbs.ledger.service.LedgerAccountService;
import com.cbs.ledger.service.LedgerPostingService;
import com.cbs.ledger.service.LedgerQueryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ledger")
@Validated
public class LedgerController {

    private final LedgerAccountService ledgerAccountService;
    private final LedgerPostingService ledgerPostingService;
    private final LedgerQueryService ledgerQueryService;

    public LedgerController(LedgerAccountService ledgerAccountService,
                            LedgerPostingService ledgerPostingService,
                            LedgerQueryService ledgerQueryService) {
        this.ledgerAccountService = ledgerAccountService;
        this.ledgerPostingService = ledgerPostingService;
        this.ledgerQueryService = ledgerQueryService;
    }

    @PostMapping("/accounts")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = ledgerAccountService.createAccount(request);
        return ResponseEntity.ok(ApiResponse.success("Account created", response));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> listAccounts() {
        return ResponseEntity.ok(ApiResponse.success("Accounts fetched", ledgerAccountService.listAccounts()));
    }

    @PostMapping("/entries")
    public ResponseEntity<ApiResponse<PostJournalEntryResponse>> postEntry(
            @Valid @RequestBody PostJournalEntryRequest request
    ) {
        PostJournalEntryResponse response = ledgerPostingService.postEntry(request);
        return ResponseEntity.ok(ApiResponse.success("Journal entry posted", response));
    }

    @GetMapping("/balances/{accountCode}")
    public ResponseEntity<ApiResponse<BalanceResponse>> getBalance(@PathVariable("accountCode") @NotBlank String accountCode) {
        BalanceResponse response = ledgerQueryService.getGlBalance(accountCode);
        return ResponseEntity.ok(ApiResponse.success("Balance fetched", response));
    }

    @GetMapping("/reconciliation")
    public ResponseEntity<ApiResponse<ReconciliationResponse>> reconcile(
            @RequestParam("fromDate") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam("toDate") @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        ReconciliationResponse response = ledgerQueryService.reconcile(fromDate, toDate);
        return ResponseEntity.ok(ApiResponse.success("Reconciliation completed", response));
    }
}
