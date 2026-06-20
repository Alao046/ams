package com.justjava.ams.auditor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.justjava.ams.common.entity.Organization;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "compliance_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private String reportName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ComplianceType complianceType;

    @Column(nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ComplianceStatus status = ComplianceStatus.DRAFT;

    @Column
    private Integer totalIssuesFound = 0;

    @Column
    private Integer criticalIssues = 0;

    @Column
    private Integer resolutionRate = 0;

    @Column(columnDefinition = "TEXT")
    private String reportContent;

    @Column
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum ComplianceType {
        INTERNAL_CONTROL,
        DATA_PROTECTION,
        FINANCIAL_REPORTING,
        REGULATORY,
        SECURITY,
        OPERATIONAL
    }

    public enum ComplianceStatus {
        DRAFT,
        PENDING_REVIEW,
        UNDER_REVIEW,
        APPROVED,
        PUBLISHED
    }
}

