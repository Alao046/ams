package com.justjava.ams.auditor.repository;

import com.justjava.ams.auditor.entity.ComplianceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, Long> {
    Optional<ComplianceReport> findByOrganizationIdAndReportName(Long organizationId, String reportName);
    List<ComplianceReport> findByOrganizationIdAndComplianceType(Long organizationId, ComplianceReport.ComplianceType complianceType);
    List<ComplianceReport> findByOrganizationIdAndStatus(Long organizationId, ComplianceReport.ComplianceStatus status);
    List<ComplianceReport> findByOrganizationIdAndReportDateBetween(Long organizationId, LocalDate fromDate, LocalDate toDate);
}

