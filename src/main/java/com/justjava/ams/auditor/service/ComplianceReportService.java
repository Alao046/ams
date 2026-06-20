package com.justjava.ams.auditor.service;

import com.justjava.ams.auditor.dto.ComplianceReportDTO;
import com.justjava.ams.auditor.entity.ComplianceReport;
import com.justjava.ams.auditor.repository.ComplianceReportRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplianceReportService {

    private final ComplianceReportRepository complianceReportRepository;
    private final OrganizationRepository organizationRepository;

    public ComplianceReportDTO createComplianceReport(Long organizationId, ComplianceReportDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        ComplianceReport report = ComplianceReport.builder()
                .organization(organization)
                .reportName(dto.getReportName())
                .complianceType(ComplianceReport.ComplianceType.valueOf(dto.getComplianceType()))
                .reportDate(dto.getReportDate())
                .fromDate(dto.getFromDate())
                .toDate(dto.getToDate())
                .totalIssuesFound(dto.getTotalIssuesFound())
                .criticalIssues(dto.getCriticalIssues())
                .resolutionRate(dto.getResolutionRate())
                .reportContent(dto.getReportContent())
                .notes(dto.getNotes())
                .build();

        return mapToDTO(complianceReportRepository.save(report));
    }

    public ComplianceReportDTO getComplianceReport(Long reportId) {
        ComplianceReport report = complianceReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Compliance report not found"));
        return mapToDTO(report);
    }

    public List<ComplianceReportDTO> getReportsByType(Long organizationId, String complianceType) {
        return complianceReportRepository.findByOrganizationIdAndComplianceType(organizationId, ComplianceReport.ComplianceType.valueOf(complianceType))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ComplianceReportDTO> getReportsByStatus(Long organizationId, String status) {
        return complianceReportRepository.findByOrganizationIdAndStatus(organizationId, ComplianceReport.ComplianceStatus.valueOf(status))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ComplianceReportDTO> getReportsByDateRange(Long organizationId, LocalDate fromDate, LocalDate toDate) {
        return complianceReportRepository.findByOrganizationIdAndReportDateBetween(organizationId, fromDate, toDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ComplianceReportDTO approveReport(Long reportId) {
        ComplianceReport report = complianceReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Compliance report not found"));
        report.setStatus(ComplianceReport.ComplianceStatus.APPROVED);
        return mapToDTO(complianceReportRepository.save(report));
    }

    private ComplianceReportDTO mapToDTO(ComplianceReport report) {
        return ComplianceReportDTO.builder()
                .id(report.getId())
                .organizationId(report.getOrganization().getId())
                .reportName(report.getReportName())
                .complianceType(report.getComplianceType().toString())
                .reportDate(report.getReportDate())
                .fromDate(report.getFromDate())
                .toDate(report.getToDate())
                .status(report.getStatus().toString())
                .totalIssuesFound(report.getTotalIssuesFound())
                .criticalIssues(report.getCriticalIssues())
                .resolutionRate(report.getResolutionRate())
                .reportContent(report.getReportContent())
                .notes(report.getNotes())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}

