package com.cbs.reporting.repository;

import com.cbs.reporting.model.GeneratedReport;
import com.cbs.reporting.model.ReportStatus;
import com.cbs.reporting.model.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {

    boolean existsByReference(String reference);

    List<GeneratedReport> findByRequestedByCustomerIdOrderByIdDesc(Long requestedByCustomerId);

    List<GeneratedReport> findByStatusOrderByIdDesc(ReportStatus status);

    List<GeneratedReport> findByReportTypeOrderByIdDesc(ReportType reportType);
}
