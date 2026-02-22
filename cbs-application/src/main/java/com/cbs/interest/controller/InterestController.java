package com.cbs.interest.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.cbs.common.api.ApiResponse;
import com.cbs.interest.dto.CreateInterestConfigRequest;
import com.cbs.interest.dto.InterestAccrualResponse;
import com.cbs.interest.dto.InterestConfigResponse;
import com.cbs.interest.dto.RunAccrualRequest;
import com.cbs.interest.dto.UpdateInterestConfigRequest;
import com.cbs.interest.model.InterestStatus;
import com.cbs.interest.service.InterestService;
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
@RequestMapping("/api/v1/interests")
@Tag(name = "Interest", description = "Endpoints for interest rate configuration and calculations")
public class InterestController {

    private final InterestService interestService;

    public InterestController(InterestService interestService) {
        this.interestService = interestService;
    }

    @PostMapping("/configs")
    public ResponseEntity<ApiResponse<InterestConfigResponse>> createConfig(
            @Valid @RequestBody CreateInterestConfigRequest request) {
        InterestConfigResponse response = interestService.createConfig(request);
        return ResponseEntity.ok(ApiResponse.success("Interest config created", response));
    }

    @PatchMapping("/configs/{productCode}")
    public ResponseEntity<ApiResponse<InterestConfigResponse>> updateConfig(
            @PathVariable("productCode") String productCode,
            @Valid @RequestBody UpdateInterestConfigRequest request) {
        InterestConfigResponse response = interestService.updateConfig(productCode, request);
        return ResponseEntity.ok(ApiResponse.success("Interest config updated", response));
    }

    @GetMapping("/configs/{productCode}")
    public ResponseEntity<ApiResponse<InterestConfigResponse>> getConfig(
            @PathVariable("productCode") String productCode) {
        InterestConfigResponse response = interestService.getConfig(productCode);
        return ResponseEntity.ok(ApiResponse.success("Interest config retrieved", response));
    }

    @GetMapping("/configs")
    public ResponseEntity<ApiResponse<List<InterestConfigResponse>>> listConfigs(
            @RequestParam(value = "status", required = false) InterestStatus status) {
        List<InterestConfigResponse> responses = interestService.listConfigs(status);
        return ResponseEntity.ok(ApiResponse.success("Interest configs retrieved", responses));
    }

    @PostMapping("/accruals")
    public ResponseEntity<ApiResponse<InterestAccrualResponse>> runAccrual(
            @Valid @RequestBody RunAccrualRequest request) {
        InterestAccrualResponse response = interestService.runAccrual(request);
        return ResponseEntity.ok(ApiResponse.success("Interest accrual completed", response));
    }

    @GetMapping("/accruals")
    public ResponseEntity<ApiResponse<List<InterestAccrualResponse>>> listAccruals(
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "productCode", required = false) String productCode) {
        List<InterestAccrualResponse> responses = interestService.listAccruals(accountId, productCode);
        return ResponseEntity.ok(ApiResponse.success("Interest accruals retrieved", responses));
    }

    @PostMapping("/jobs/accrual:trigger")
    public ResponseEntity<ApiResponse<Integer>> triggerDailyAccrual(
            @RequestParam(value = "date", required = false) java.time.LocalDate date) {
        java.time.LocalDate processDate = date != null ? date : java.time.LocalDate.now();
        int count = interestService.calculateDailyAccrualsForAllAccounts(processDate);
        return ResponseEntity.ok(ApiResponse.success("Daily interest accrual triggered successfully", count));
    }

    @PostMapping("/jobs/capitalization:trigger")
    public ResponseEntity<ApiResponse<Integer>> triggerMonthlyCapitalization(
            @RequestParam(value = "date", required = false) java.time.LocalDate date) {
        java.time.LocalDate processDate = date != null ? date : java.time.LocalDate.now();
        int count = interestService.capitalizeMonthlyAccruals(processDate);
        return ResponseEntity.ok(ApiResponse.success("Monthly interest capitalization triggered successfully", count));
    }
}
