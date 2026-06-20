package com.justjava.ams.cfo.repository;

import com.justjava.ams.cfo.entity.FinancialReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialReportRepository extends JpaRepository<FinancialReport, Long> {
    Optional<FinancialReport> findByOrganizationIdAndReportName(Long organizationId, String reportName);
    List<FinancialReport> findByOrganizationIdAndReportType(Long organizationId, FinancialReport.ReportType reportType);
    List<FinancialReport> findByOrganizationIdAndStatus(Long organizationId, FinancialReport.ReportStatus status);
    List<FinancialReport> findByOrganizationIdAndReportDateBetween(Long organizationId, LocalDate fromDate, LocalDate toDate);
}

