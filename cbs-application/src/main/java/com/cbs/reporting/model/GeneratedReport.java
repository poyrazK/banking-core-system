package com.cbs.reporting.model;

import com.cbs.common.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(
        name = "generated_reports",
        uniqueConstraints = @UniqueConstraint(name = "uk_generated_reports_reference", columnNames = "reference")
)
public class GeneratedReport extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long requestedByCustomerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReportType reportType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReportFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReportStatus status = ReportStatus.REQUESTED;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Column(nullable = false, length = 64)
    private String reference;

    @Column(length = 1024)
    private String outputLocation;

    @Column(length = 255)
    private String statusReason;

    public GeneratedReport() {
    }

    public GeneratedReport(Long requestedByCustomerId,
                           ReportType reportType,
                           ReportFormat format,
                           LocalDate fromDate,
                           LocalDate toDate,
                           String reference) {
        this.requestedByCustomerId = requestedByCustomerId;
        this.reportType = reportType;
        this.format = format;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.reference = reference;
    }

    public Long getId() {
        return id;
    }

    public Long getRequestedByCustomerId() {
        return requestedByCustomerId;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public ReportFormat getFormat() {
        return format;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public String getReference() {
        return reference;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public void setOutputLocation(String outputLocation) {
        this.outputLocation = outputLocation;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }
}
