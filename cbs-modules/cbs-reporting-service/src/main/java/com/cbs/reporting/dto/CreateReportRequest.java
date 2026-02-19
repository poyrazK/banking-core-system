package com.cbs.reporting.dto;

import com.cbs.reporting.model.ReportFormat;
import com.cbs.reporting.model.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateReportRequest(
        @NotNull Long requestedByCustomerId,
        @NotNull ReportType reportType,
        @NotNull ReportFormat format,
        @NotNull LocalDate fromDate,
        @NotNull LocalDate toDate,
        @NotBlank @Size(max = 64) String reference
) {
}
