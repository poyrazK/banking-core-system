package com.cbs.reporting.dto;

import com.cbs.reporting.model.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateReportStatusRequest(
        @NotNull ReportStatus status,
        @Size(max = 1024) String outputLocation,
        @NotBlank @Size(max = 255) String reason
) {
}
