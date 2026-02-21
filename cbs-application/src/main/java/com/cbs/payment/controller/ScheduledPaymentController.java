package com.cbs.payment.controller;

import com.cbs.common.api.ApiResponse;
import com.cbs.payment.dto.CreateScheduledPaymentRequest;
import com.cbs.payment.dto.PaymentStatusUpdateRequest;
import com.cbs.payment.dto.ScheduledPaymentResponse;
import com.cbs.payment.model.ScheduledPaymentStatus;
import com.cbs.payment.service.ScheduledPaymentService;
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
@RequestMapping("/api/v1/scheduled-payments")
@Tag(name = "Scheduled Payments", description = "Endpoints for managing recurring payments and standing orders")
public class ScheduledPaymentController {

    private final ScheduledPaymentService scheduledPaymentService;

    public ScheduledPaymentController(ScheduledPaymentService scheduledPaymentService) {
        this.scheduledPaymentService = scheduledPaymentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> createScheduledPayment(
            @Valid @RequestBody CreateScheduledPaymentRequest request) {
        ScheduledPaymentResponse response = scheduledPaymentService.createScheduledPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payment created", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> getScheduledPayment(@PathVariable("id") Long id) {
        ScheduledPaymentResponse response = scheduledPaymentService.getScheduledPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payment retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScheduledPaymentResponse>>> listScheduledPayments(
            @RequestParam(value = "customerId", required = false) Long customerId,
            @RequestParam(value = "status", required = false) ScheduledPaymentStatus status) {
        List<ScheduledPaymentResponse> responses = scheduledPaymentService.listScheduledPayments(customerId, status);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payments retrieved", responses));
    }

    @PatchMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> pauseScheduledPayment(@PathVariable("id") Long id) {
        ScheduledPaymentResponse response = scheduledPaymentService.pauseScheduledPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payment paused", response));
    }

    @PatchMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> resumeScheduledPayment(@PathVariable("id") Long id) {
        ScheduledPaymentResponse response = scheduledPaymentService.resumeScheduledPayment(id);
        return ResponseEntity.ok(ApiResponse.success("Scheduled payment resumed", response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ScheduledPaymentResponse>> cancelScheduledPayment(
            @PathVariable("id") Long id,
            @Valid @RequestBody PaymentStatusUpdateRequest request) {
        ScheduledPaymentResponse response = scheduledPaymentService.cancelScheduledPayment(id, request.reason());
        return ResponseEntity.ok(ApiResponse.success("Scheduled payment cancelled", response));
    }
}
