package com.cbs.reporting.service;

import com.cbs.common.exception.ApiException;
import com.cbs.reporting.dto.CreateReportRequest;
import com.cbs.reporting.dto.ReportResponse;
import com.cbs.reporting.dto.UpdateReportStatusRequest;
import com.cbs.reporting.model.GeneratedReport;
import com.cbs.reporting.model.ReportFormat;
import com.cbs.reporting.model.ReportStatus;
import com.cbs.reporting.model.ReportType;
import com.cbs.reporting.repository.GeneratedReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingServiceTest {

    @Mock
    private GeneratedReportRepository generatedReportRepository;

    private ReportingService reportingService;

    @BeforeEach
    void setUp() {
        reportingService = new ReportingService(generatedReportRepository);
    }

    @Test
    void createReport_normalizesReferenceAndGeneratesOutputLocation() {
        CreateReportRequest request = new CreateReportRequest(
                1L,
                ReportType.ACCOUNT_STATEMENT,
                ReportFormat.PDF,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "  rpt-01  "
        );
        when(generatedReportRepository.existsByReference("RPT-01")).thenReturn(false);
        when(generatedReportRepository.save(any(GeneratedReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse response = reportingService.createReport(request);

        assertEquals("RPT-01", response.reference());
        assertEquals(ReportStatus.GENERATED, response.status());
        assertEquals("/reports/RPT-01.pdf", response.outputLocation());
    }

    @Test
    void createReport_throwsWhenReferenceExists() {
        CreateReportRequest request = new CreateReportRequest(
                1L,
                ReportType.ACCOUNT_STATEMENT,
                ReportFormat.PDF,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "RPT-01"
        );
        when(generatedReportRepository.existsByReference("RPT-01")).thenReturn(true);

        ApiException exception = assertThrows(ApiException.class, () -> reportingService.createReport(request));

        assertEquals("REPORT_REFERENCE_EXISTS", exception.getErrorCode());
    }

    @Test
    void updateReportStatus_throwsWhenCancelled() {
        GeneratedReport report = createReportWithStatus(ReportStatus.CANCELLED);
        when(generatedReportRepository.findById(11L)).thenReturn(Optional.of(report));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> reportingService.updateReportStatus(11L, new UpdateReportStatusRequest(ReportStatus.GENERATED, "/reports/x.pdf", "retry"))
        );

        assertEquals("REPORT_CANCELLED", exception.getErrorCode());
    }

    @Test
    void cancelReport_setsCancelledStatusAndReason() {
        GeneratedReport report = createReportWithStatus(ReportStatus.REQUESTED);
        when(generatedReportRepository.findById(12L)).thenReturn(Optional.of(report));
        when(generatedReportRepository.save(any(GeneratedReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReportResponse response = reportingService.cancelReport(
                12L,
                new UpdateReportStatusRequest(ReportStatus.CANCELLED, null, "  user request  ")
        );

        assertEquals(ReportStatus.CANCELLED, response.status());
        assertEquals("user request", response.statusReason());
    }

    @Test
    void getReport_throwsWhenMissing() {
        when(generatedReportRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> reportingService.getReport(99L));

        assertEquals("REPORT_NOT_FOUND", exception.getErrorCode());
    }

    private GeneratedReport createReportWithStatus(ReportStatus status) {
        GeneratedReport report = new GeneratedReport(
                1L,
                ReportType.ACCOUNT_STATEMENT,
                ReportFormat.PDF,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "RPT-01"
        );
        report.setStatus(status);
        return report;
    }
}
