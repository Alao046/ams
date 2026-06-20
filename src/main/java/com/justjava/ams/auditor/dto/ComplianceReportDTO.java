package com.justjava.ams.auditor.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceReportDTO {

    private Long id;
    private Long organizationId;
    private String reportName;
    private String complianceType;
    private LocalDate reportDate;
    private LocalDate fromDate;
    private LocalDate toDate;
    private String status;
    private Integer totalIssuesFound;
    private Integer criticalIssues;
    private Integer resolutionRate;
    private String reportContent;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

