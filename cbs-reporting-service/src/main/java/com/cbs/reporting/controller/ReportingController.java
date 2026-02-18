package com.cbs.reporting.controller;

import com.cbs.common.api.ApiResponse;
import com.cbs.reporting.dto.CreateReportRequest;
import com.cbs.reporting.dto.ReportResponse;
import com.cbs.reporting.dto.UpdateReportStatusRequest;
import com.cbs.reporting.model.ReportStatus;
import com.cbs.reporting.model.ReportType;
import com.cbs.reporting.service.ReportingService;
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
@RequestMapping("/api/v1/reports")
public class ReportingController {

    private final ReportingService reportingService;

    public ReportingController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(@Valid @RequestBody CreateReportRequest request) {
        ReportResponse response = reportingService.createReport(request);
        return ResponseEntity.ok(ApiResponse.success("Report created", response));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponse<ReportResponse>> getReport(@PathVariable("reportId") Long reportId) {
        ReportResponse response = reportingService.getReport(reportId);
        return ResponseEntity.ok(ApiResponse.success("Report retrieved", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReportResponse>>> listReports(
            @RequestParam(value = "requestedByCustomerId", required = false) Long requestedByCustomerId,
            @RequestParam(value = "reportType", required = false) ReportType reportType,
            @RequestParam(value = "status", required = false) ReportStatus status
    ) {
        List<ReportResponse> responses = reportingService.listReports(requestedByCustomerId, reportType, status);
        return ResponseEntity.ok(ApiResponse.success("Reports retrieved", responses));
    }

    @PatchMapping("/{reportId}/status")
    public ResponseEntity<ApiResponse<ReportResponse>> updateStatus(
            @PathVariable("reportId") Long reportId,
            @Valid @RequestBody UpdateReportStatusRequest request
    ) {
        ReportResponse response = reportingService.updateReportStatus(reportId, request);
        return ResponseEntity.ok(ApiResponse.success("Report status updated", response));
    }

    @PatchMapping("/{reportId}/cancel")
    public ResponseEntity<ApiResponse<ReportResponse>> cancelReport(
            @PathVariable("reportId") Long reportId,
            @Valid @RequestBody UpdateReportStatusRequest request
    ) {
        ReportResponse response = reportingService.cancelReport(reportId, request);
        return ResponseEntity.ok(ApiResponse.success("Report cancelled", response));
    }
}
