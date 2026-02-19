package com.cbs.deposit.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.cbs.common.api.ApiResponse;
import com.cbs.deposit.dto.AccrueInterestRequest;
import com.cbs.deposit.dto.CreateDepositRequest;
import com.cbs.deposit.dto.DepositResponse;
import com.cbs.deposit.dto.DepositStatusReasonRequest;
import com.cbs.deposit.model.DepositStatus;
import com.cbs.deposit.service.DepositService;
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
@RequestMapping("/api/v1/deposits")
@Tag(name = "Deposits", description = "Endpoints for fixed and recurring deposit management")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DepositResponse>> createDeposit(
            @Valid @RequestBody CreateDepositRequest request) {
        DepositResponse response = depositService.createDeposit(request);
        return ResponseEntity.ok(ApiResponse.success("Deposit account created", response));
    }

    @GetMapping("/{depositId}")
    public ResponseEntity<ApiResponse<DepositResponse>> getDeposit(@PathVariable("depositId") Long depositId) {
        DepositResponse response = depositService.getDeposit(depositId);
        return ResponseEntity.ok(ApiResponse.success("Deposit account retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DepositResponse>>> listDeposits(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "status", required = false) DepositStatus status) {
        List<DepositResponse> responses = depositService.listDeposits(customerId, status);
        return ResponseEntity.ok(ApiResponse.success("Deposit accounts retrieved", responses));
    }

    @PatchMapping("/{depositId}/accrue")
    public ResponseEntity<ApiResponse<DepositResponse>> accrueInterest(
            @PathVariable("depositId") Long depositId,
            @Valid @RequestBody AccrueInterestRequest request) {
        DepositResponse response = depositService.accrueInterest(depositId, request);
        return ResponseEntity.ok(ApiResponse.success("Interest accrued", response));
    }

    @PatchMapping("/{depositId}/mature")
    public ResponseEntity<ApiResponse<DepositResponse>> matureDeposit(@PathVariable("depositId") Long depositId) {
        DepositResponse response = depositService.matureDeposit(depositId);
        return ResponseEntity.ok(ApiResponse.success("Deposit matured", response));
    }

    @PatchMapping("/{depositId}/close")
    public ResponseEntity<ApiResponse<DepositResponse>> closeDeposit(@PathVariable("depositId") Long depositId) {
        DepositResponse response = depositService.closeDeposit(depositId);
        return ResponseEntity.ok(ApiResponse.success("Deposit closed", response));
    }

    @PatchMapping("/{depositId}/break")
    public ResponseEntity<ApiResponse<DepositResponse>> breakDeposit(
            @PathVariable("depositId") Long depositId,
            @Valid @RequestBody DepositStatusReasonRequest request) {
        DepositResponse response = depositService.breakDeposit(depositId, request);
        return ResponseEntity.ok(ApiResponse.success("Deposit broken", response));
    }
}
