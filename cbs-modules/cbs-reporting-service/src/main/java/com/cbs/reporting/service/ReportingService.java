package com.cbs.reporting.service;

import com.cbs.common.exception.ApiException;
import com.cbs.reporting.dto.CreateReportRequest;
import com.cbs.reporting.dto.ReportResponse;
import com.cbs.reporting.dto.UpdateReportStatusRequest;
import com.cbs.reporting.model.GeneratedReport;
import com.cbs.reporting.model.ReportStatus;
import com.cbs.reporting.model.ReportType;
import com.cbs.reporting.repository.GeneratedReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ReportingService {

    private final GeneratedReportRepository generatedReportRepository;

    public ReportingService(GeneratedReportRepository generatedReportRepository) {
        this.generatedReportRepository = generatedReportRepository;
    }

    @Transactional
    public ReportResponse createReport(CreateReportRequest request) {
        if (request.fromDate().isAfter(request.toDate())) {
            throw new ApiException("REPORT_INVALID_DATE_RANGE", "fromDate must be before or equal to toDate");
        }

        String reference = normalizeReference(request.reference());
        if (generatedReportRepository.existsByReference(reference)) {
            throw new ApiException("REPORT_REFERENCE_EXISTS", "Reference already exists");
        }

        GeneratedReport report = new GeneratedReport(
                request.requestedByCustomerId(),
                request.reportType(),
                request.format(),
                request.fromDate(),
                request.toDate(),
                reference
        );
        report.setStatus(ReportStatus.GENERATED);
        report.setOutputLocation("/reports/" + reference + "." + request.format().name().toLowerCase());

        return ReportResponse.from(generatedReportRepository.save(report));
    }

    @Transactional(readOnly = true)
    public ReportResponse getReport(Long reportId) {
        return ReportResponse.from(findReport(reportId));
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> listReports(Long requestedByCustomerId, ReportType reportType, ReportStatus status) {
        List<GeneratedReport> reports;

        if (requestedByCustomerId != null && reportType == null && status == null) {
            reports = generatedReportRepository.findByRequestedByCustomerIdOrderByIdDesc(requestedByCustomerId);
        } else if (reportType != null && requestedByCustomerId == null && status == null) {
            reports = generatedReportRepository.findByReportTypeOrderByIdDesc(reportType);
        } else if (status != null && requestedByCustomerId == null && reportType == null) {
            reports = generatedReportRepository.findByStatusOrderByIdDesc(status);
        } else {
            reports = generatedReportRepository.findAll().stream()
                    .filter(report -> requestedByCustomerId == null || report.getRequestedByCustomerId().equals(requestedByCustomerId))
                    .filter(report -> reportType == null || report.getReportType() == reportType)
                    .filter(report -> status == null || report.getStatus() == status)
                    .sorted(Comparator.comparing(GeneratedReport::getId).reversed())
                    .toList();
        }

        return reports.stream().map(ReportResponse::from).toList();
    }

    @Transactional
    public ReportResponse updateReportStatus(Long reportId, UpdateReportStatusRequest request) {
        GeneratedReport report = findReport(reportId);

        if (report.getStatus() == ReportStatus.CANCELLED) {
            throw new ApiException("REPORT_CANCELLED", "Cancelled report cannot be updated");
        }

        report.setStatus(request.status());
        report.setOutputLocation(request.outputLocation());
        report.setStatusReason(request.reason().trim());

        return ReportResponse.from(generatedReportRepository.save(report));
    }

    @Transactional
    public ReportResponse cancelReport(Long reportId, UpdateReportStatusRequest request) {
        GeneratedReport report = findReport(reportId);
        if (report.getStatus() == ReportStatus.CANCELLED) {
            throw new ApiException("REPORT_ALREADY_CANCELLED", "Report is already cancelled");
        }

        report.setStatus(ReportStatus.CANCELLED);
        report.setStatusReason(request.reason().trim());
        return ReportResponse.from(generatedReportRepository.save(report));
    }

    private GeneratedReport findReport(Long reportId) {
        return generatedReportRepository.findById(reportId)
                .orElseThrow(() -> new ApiException("REPORT_NOT_FOUND", "Report not found"));
    }

    private String normalizeReference(String reference) {
        return reference.trim().toUpperCase();
    }
}
