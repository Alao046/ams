package com.justjava.ams.cfo.service;

import com.justjava.ams.cfo.dto.FinancialReportDTO;
import com.justjava.ams.cfo.entity.FinancialReport;
import com.justjava.ams.cfo.repository.FinancialReportRepository;
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
public class FinancialReportService {

    private final FinancialReportRepository financialReportRepository;
    private final OrganizationRepository organizationRepository;

    public FinancialReportDTO createReport(Long organizationId, FinancialReportDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        FinancialReport report = FinancialReport.builder()
                .organization(organization)
                .reportType(FinancialReport.ReportType.valueOf(dto.getReportType()))
                .reportName(dto.getReportName())
                .reportDate(dto.getReportDate())
                .fromDate(dto.getFromDate())
                .toDate(dto.getToDate())
                .generatedBy(dto.getGeneratedBy())
                .reportContent(dto.getReportContent())
                .notes(dto.getNotes())
                .build();

        return mapToDTO(financialReportRepository.save(report));
    }

    public FinancialReportDTO getReport(Long reportId) {
        FinancialReport report = financialReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        return mapToDTO(report);
    }

    public List<FinancialReportDTO> getReportsByType(Long organizationId, String reportType) {
        return financialReportRepository.findByOrganizationIdAndReportType(organizationId, FinancialReport.ReportType.valueOf(reportType))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<FinancialReportDTO> getReportsByDateRange(Long organizationId, LocalDate fromDate, LocalDate toDate) {
        return financialReportRepository.findByOrganizationIdAndReportDateBetween(organizationId, fromDate, toDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FinancialReportDTO approveReport(Long reportId, String approvedBy) {
        FinancialReport report = financialReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(FinancialReport.ReportStatus.APPROVED);
        report.setApprovedBy(approvedBy);
        report.setApprovedDate(LocalDate.now());
        return mapToDTO(financialReportRepository.save(report));
    }

    private FinancialReportDTO mapToDTO(FinancialReport report) {
        return FinancialReportDTO.builder()
                .id(report.getId())
                .organizationId(report.getOrganization().getId())
                .reportType(report.getReportType().toString())
                .reportName(report.getReportName())
                .reportDate(report.getReportDate())
                .fromDate(report.getFromDate())
                .toDate(report.getToDate())
                .status(report.getStatus().toString())
                .reportContent(report.getReportContent())
                .generatedBy(report.getGeneratedBy())
                .approvedDate(report.getApprovedDate())
                .approvedBy(report.getApprovedBy())
                .notes(report.getNotes())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}

