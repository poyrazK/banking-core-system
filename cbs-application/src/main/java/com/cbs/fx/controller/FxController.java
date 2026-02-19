package com.cbs.fx.controller;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.cbs.common.api.ApiResponse;
import com.cbs.fx.dto.BookFxDealRequest;
import com.cbs.fx.dto.CancelFxDealRequest;
import com.cbs.fx.dto.CreateFxQuoteRequest;
import com.cbs.fx.dto.CreateFxRateRequest;
import com.cbs.fx.dto.FxDealResponse;
import com.cbs.fx.dto.FxQuoteResponse;
import com.cbs.fx.dto.FxRateResponse;
import com.cbs.fx.dto.UpdateFxRateRequest;
import com.cbs.fx.model.FxDealStatus;
import com.cbs.fx.model.FxRateStatus;
import com.cbs.fx.service.FxService;
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
@RequestMapping("/api/v1/fx")
@Tag(name = "Foreign Exchange", description = "Endpoints for currency exchange rates and conversion")
public class FxController {

    private final FxService fxService;

    public FxController(FxService fxService) {
        this.fxService = fxService;
    }

    @PostMapping("/rates")
    public ResponseEntity<ApiResponse<FxRateResponse>> createRate(@Valid @RequestBody CreateFxRateRequest request) {
        FxRateResponse response = fxService.createRate(request);
        return ResponseEntity.ok(ApiResponse.success("FX rate created", response));
    }

    @PatchMapping("/rates/{currencyPair}")
    public ResponseEntity<ApiResponse<FxRateResponse>> updateRate(
            @PathVariable("currencyPair") String currencyPair,
            @Valid @RequestBody UpdateFxRateRequest request) {
        FxRateResponse response = fxService.updateRate(currencyPair, request);
        return ResponseEntity.ok(ApiResponse.success("FX rate updated", response));
    }

    @GetMapping("/rates/{currencyPair}")
    public ResponseEntity<ApiResponse<FxRateResponse>> getRate(@PathVariable("currencyPair") String currencyPair) {
        FxRateResponse response = fxService.getRate(currencyPair);
        return ResponseEntity.ok(ApiResponse.success("FX rate retrieved", response));
    }

    @GetMapping("/rates")
    public ResponseEntity<ApiResponse<List<FxRateResponse>>> listRates(
            @RequestParam(value = "status", required = false) FxRateStatus status) {
        List<FxRateResponse> responses = fxService.listRates(status);
        return ResponseEntity.ok(ApiResponse.success("FX rates retrieved", responses));
    }

    @PostMapping("/quotes")
    public ResponseEntity<ApiResponse<FxQuoteResponse>> createQuote(
            @Valid @RequestBody CreateFxQuoteRequest request) {
        FxQuoteResponse response = fxService.createQuote(request);
        return ResponseEntity.ok(ApiResponse.success("FX quote generated", response));
    }

    @PostMapping("/deals")
    public ResponseEntity<ApiResponse<FxDealResponse>> bookDeal(
            @Valid @RequestBody BookFxDealRequest request) {
        FxDealResponse response = fxService.bookDeal(request);
        return ResponseEntity.ok(ApiResponse.success("FX deal booked", response));
    }

    @GetMapping("/deals/{dealId}")
    public ResponseEntity<ApiResponse<FxDealResponse>> getDeal(@PathVariable("dealId") Long dealId) {
        FxDealResponse response = fxService.getDeal(dealId);
        return ResponseEntity.ok(ApiResponse.success("FX deal retrieved", response));
    }

    @GetMapping("/deals")
    public ResponseEntity<ApiResponse<List<FxDealResponse>>> listDeals(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "status", required = false) FxDealStatus status) {
        List<FxDealResponse> responses = fxService.listDeals(customerId, status);
        return ResponseEntity.ok(ApiResponse.success("FX deals retrieved", responses));
    }

    @PatchMapping("/deals/{dealId}/cancel")
    public ResponseEntity<ApiResponse<FxDealResponse>> cancelDeal(
            @PathVariable("dealId") Long dealId,
            @Valid @RequestBody CancelFxDealRequest request) {
        FxDealResponse response = fxService.cancelDeal(dealId, request);
        return ResponseEntity.ok(ApiResponse.success("FX deal cancelled", response));
    }
}
