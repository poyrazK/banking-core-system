package com.cbs.reporting.dto;

import com.cbs.reporting.model.GeneratedReport;
import com.cbs.reporting.model.ReportFormat;
import com.cbs.reporting.model.ReportStatus;
import com.cbs.reporting.model.ReportType;

import java.time.LocalDate;

public record ReportResponse(
        Long id,
        Long requestedByCustomerId,
        ReportType reportType,
        ReportFormat format,
        ReportStatus status,
        LocalDate fromDate,
        LocalDate toDate,
        String reference,
        String outputLocation,
        String statusReason
) {
    public static ReportResponse from(GeneratedReport report) {
        return new ReportResponse(
                report.getId(),
                report.getRequestedByCustomerId(),
                report.getReportType(),
                report.getFormat(),
                report.getStatus(),
                report.getFromDate(),
                report.getToDate(),
                report.getReference(),
                report.getOutputLocation(),
                report.getStatusReason()
        );
    }
}
