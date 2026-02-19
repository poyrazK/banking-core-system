package com.cbs.reporting.controller;

import com.cbs.common.exception.ApiException;
import com.cbs.reporting.dto.ReportResponse;
import com.cbs.reporting.exception.ReportingExceptionHandler;
import com.cbs.reporting.model.ReportFormat;
import com.cbs.reporting.model.ReportStatus;
import com.cbs.reporting.model.ReportType;
import com.cbs.reporting.service.ReportingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportingController.class)
@Import(ReportingExceptionHandler.class)
class ReportingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportingService reportingService;

    @Test
    void createReport_returnsSuccessResponse() throws Exception {
        ReportResponse response = new ReportResponse(
                1L,
                10L,
                ReportType.ACCOUNT_STATEMENT,
                ReportFormat.PDF,
                ReportStatus.GENERATED,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                "RPT-01",
                "/reports/RPT-01.pdf",
                null
        );
        when(reportingService.createReport(any())).thenReturn(response);

        String body = """
                {
                  "requestedByCustomerId": 10,
                  "reportType": "ACCOUNT_STATEMENT",
                  "format": "PDF",
                  "fromDate": "2026-01-01",
                  "toDate": "2026-01-31",
                  "reference": "RPT-01"
                }
                """;

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Report created"))
                .andExpect(jsonPath("$.data.reference").value("RPT-01"));
    }

    @Test
    void createReport_returnsBadRequestWhenPayloadIsInvalid() throws Exception {
        String body = """
                {
                  "requestedByCustomerId": 10,
                  "format": "PDF"
                }
                """;

        mockMvc.perform(post("/api/v1/reports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateStatus_returnsBusinessErrorWhenCancelled() throws Exception {
        when(reportingService.updateReportStatus(any(), any()))
                .thenThrow(new ApiException("REPORT_CANCELLED", "Cancelled report cannot be updated"));

        String body = """
                {
                  "status": "GENERATED",
                  "outputLocation": "/reports/RPT-01.pdf",
                  "reason": "retry"
                }
                """;

        mockMvc.perform(patch("/api/v1/reports/{reportId}/status", 9)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cancelled report cannot be updated"));
    }
}
