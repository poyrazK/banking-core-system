package com.cbs.fee.controller;

import com.cbs.common.api.ApiResponse;
import com.cbs.fee.dto.ChargeFeeRequest;
import com.cbs.fee.dto.CreateFeeConfigRequest;
import com.cbs.fee.dto.FeeChargeResponse;
import com.cbs.fee.dto.FeeConfigResponse;
import com.cbs.fee.dto.UpdateFeeConfigRequest;
import com.cbs.fee.model.FeeStatus;
import com.cbs.fee.service.FeeService;
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
@RequestMapping("/api/v1/fees")
public class FeeController {

    private final FeeService feeService;

    public FeeController(FeeService feeService) {
        this.feeService = feeService;
    }

    @PostMapping("/configs")
    public ResponseEntity<ApiResponse<FeeConfigResponse>> createConfig(
            @Valid @RequestBody CreateFeeConfigRequest request
    ) {
        FeeConfigResponse response = feeService.createConfig(request);
        return ResponseEntity.ok(ApiResponse.success("Fee config created", response));
    }

    @PatchMapping("/configs/{feeCode}")
    public ResponseEntity<ApiResponse<FeeConfigResponse>> updateConfig(
            @PathVariable("feeCode") String feeCode,
            @Valid @RequestBody UpdateFeeConfigRequest request
    ) {
        FeeConfigResponse response = feeService.updateConfig(feeCode, request);
        return ResponseEntity.ok(ApiResponse.success("Fee config updated", response));
    }

    @GetMapping("/configs/{feeCode}")
    public ResponseEntity<ApiResponse<FeeConfigResponse>> getConfig(@PathVariable("feeCode") String feeCode) {
        FeeConfigResponse response = feeService.getConfig(feeCode);
        return ResponseEntity.ok(ApiResponse.success("Fee config retrieved", response));
    }

    @GetMapping("/configs")
    public ResponseEntity<ApiResponse<List<FeeConfigResponse>>> listConfigs(
            @RequestParam(value = "status", required = false) FeeStatus status
    ) {
        List<FeeConfigResponse> responses = feeService.listConfigs(status);
        return ResponseEntity.ok(ApiResponse.success("Fee configs retrieved", responses));
    }

    @PostMapping("/charges")
    public ResponseEntity<ApiResponse<FeeChargeResponse>> chargeFee(@Valid @RequestBody ChargeFeeRequest request) {
        FeeChargeResponse response = feeService.chargeFee(request);
        return ResponseEntity.ok(ApiResponse.success("Fee charged", response));
    }

    @GetMapping("/charges")
    public ResponseEntity<ApiResponse<List<FeeChargeResponse>>> listCharges(
            @RequestParam(value = "accountId", required = false) Long accountId,
            @RequestParam(value = "feeCode", required = false) String feeCode
    ) {
        List<FeeChargeResponse> responses = feeService.listCharges(accountId, feeCode);
        return ResponseEntity.ok(ApiResponse.success("Fee charges retrieved", responses));
    }
}
