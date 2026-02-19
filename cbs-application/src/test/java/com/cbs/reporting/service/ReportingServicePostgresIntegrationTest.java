package com.cbs.reporting.service;

import com.cbs.reporting.dto.CreateReportRequest;
import com.cbs.reporting.dto.ReportResponse;
import com.cbs.reporting.dto.UpdateReportStatusRequest;
import com.cbs.reporting.model.GeneratedReport;
import com.cbs.reporting.model.ReportFormat;
import com.cbs.reporting.model.ReportStatus;
import com.cbs.reporting.model.ReportType;
import com.cbs.reporting.repository.GeneratedReportRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ReportingServicePostgresIntegrationTest {

    @DynamicPropertySource
    static void registerDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> System.getProperty(
                "it.db.url",
                "jdbc:postgresql://localhost:55443/cbs_reporting_it"
        ));
        registry.add("spring.datasource.username", () -> System.getProperty("it.db.username", "test"));
        registry.add("spring.datasource.password", () -> System.getProperty("it.db.password", "test"));
    }

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private GeneratedReportRepository generatedReportRepository;

    @AfterEach
    void cleanUp() {
        generatedReportRepository.deleteAll();
    }

    @Test
    void createReportPersistsNormalizedReferenceInPostgres() {
        ReportResponse response = reportingService.createReport(new CreateReportRequest(
                1L,
                ReportType.ACCOUNT_STATEMENT,
                ReportFormat.PDF,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 1, 31),
                " rpt-01 "
        ));

        assertEquals("RPT-01", response.reference());
        assertTrue(generatedReportRepository.existsByReference("RPT-01"));
    }

    @Test
    void cancelReportPersistsStatusAndReason() {
        ReportResponse created = reportingService.createReport(new CreateReportRequest(
                2L,
                ReportType.TRANSACTION_SUMMARY,
                ReportFormat.CSV,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 2, 18),
                "RPT-02"
        ));

        ReportResponse cancelled = reportingService.cancelReport(
                created.id(),
                new UpdateReportStatusRequest(ReportStatus.CANCELLED, null, " user request ")
        );
        assertEquals(ReportStatus.CANCELLED, cancelled.status());

        GeneratedReport persisted = generatedReportRepository.findById(created.id()).orElseThrow();
        assertEquals(ReportStatus.CANCELLED, persisted.getStatus());
        assertEquals("user request", persisted.getStatusReason());
    }
}