package com.cbs.payment.controller;

import com.cbs.common.api.ApiResponse;
import com.cbs.payment.dto.CreatePaymentRequest;
import com.cbs.payment.dto.PaymentResponse;
import com.cbs.payment.dto.PaymentStatusUpdateRequest;
import com.cbs.payment.model.PaymentStatus;
import com.cbs.payment.service.PaymentService;
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
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment created", response));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(@PathVariable("paymentId") Long paymentId) {
        PaymentResponse response = paymentService.getPayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> listPayments(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "sourceAccountId", required = false) Long sourceAccountId,
            @RequestParam(value = "destinationAccountId", required = false) Long destinationAccountId,
            @RequestParam(value = "status", required = false) PaymentStatus status
    ) {
        List<PaymentResponse> responses = paymentService.listPayments(customerId, sourceAccountId, destinationAccountId, status);
        return ResponseEntity.ok(ApiResponse.success("Payments retrieved", responses));
    }

    @PatchMapping("/{paymentId}/complete")
    public ResponseEntity<ApiResponse<PaymentResponse>> completePayment(@PathVariable("paymentId") Long paymentId) {
        PaymentResponse response = paymentService.completePayment(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment completed", response));
    }

    @PatchMapping("/{paymentId}/fail")
    public ResponseEntity<ApiResponse<PaymentResponse>> failPayment(
            @PathVariable("paymentId") Long paymentId,
            @Valid @RequestBody PaymentStatusUpdateRequest request
    ) {
        PaymentResponse response = paymentService.failPayment(paymentId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment failed", response));
    }

    @PatchMapping("/{paymentId}/cancel")
    public ResponseEntity<ApiResponse<PaymentResponse>> cancelPayment(
            @PathVariable("paymentId") Long paymentId,
            @Valid @RequestBody PaymentStatusUpdateRequest request
    ) {
        PaymentResponse response = paymentService.cancelPayment(paymentId, request);
        return ResponseEntity.ok(ApiResponse.success("Payment cancelled", response));
    }

    @PatchMapping("/{paymentId}/retry-posting")
    public ResponseEntity<ApiResponse<PaymentResponse>> retryPosting(@PathVariable("paymentId") Long paymentId) {
        PaymentResponse response = paymentService.retryPosting(paymentId);
        return ResponseEntity.ok(ApiResponse.success("Payment posting retried", response));
    }
}
