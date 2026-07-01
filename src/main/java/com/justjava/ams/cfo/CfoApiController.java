package com.justjava.ams.cfo;

import com.justjava.ams.accountant.dto.ManualJournalDTO;
import com.justjava.ams.accountant.service.ManualJournalService;
import com.justjava.ams.cfo.dto.*;
import com.justjava.ams.cfo.service.FinancialReportService;
import com.justjava.ams.cfo.service.TrialBalanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cfo")
@RequiredArgsConstructor
public class CfoApiController {
    private final ManualJournalService manualJournalService;
    private final FinancialReportService financialReportService;
    private final TrialBalanceService trialBalanceService;

    @GetMapping("/manual-journals/org/{organizationId}/pending")
    public List<PendingJournalApprovalResponse> getPendingManualJournals(@PathVariable Long organizationId) {
        return manualJournalService.getPendingJournals(organizationId)
                .stream()
                .map(this::toPendingJournalApprovalResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/manual-journals/{journalId}")
    public ManualJournalDTO getManualJournalForReview(@PathVariable Long journalId) {
        return manualJournalService.getJournal(journalId);
    }

    @PatchMapping("/manual-journals/{journalId}/approve")
    public ManualJournalDTO approveManualJournal(
            @PathVariable Long journalId,
            @Valid @RequestBody(required = false) JournalApprovalRequest request,
            Principal principal) {
        JournalApprovalRequest approvalRequest = request != null ? request : new JournalApprovalRequest();
        String approverName = getUserName(principal);
        if ((approverName == null || approverName.trim().isEmpty())
                && approvalRequest.getApprovedBy() != null
                && !approvalRequest.getApprovedBy().trim().isEmpty()) {
            approverName = approvalRequest.getApprovedBy();
        }
        return manualJournalService.approveJournal(journalId, approvalRequest, approverName);
    }

    @PostMapping("/financial-reports/org/{organizationId}/generate")
    public FinancialReportSummaryResponse generateFinancialReport(
            @PathVariable Long organizationId,
            @Valid @RequestBody FinancialReportGenerateRequest request,
            Principal principal) {
        // Default persist to true if null
        if (request.getPersist() == null) {
            request.setPersist(true);
        }

        String generatedBy = getUserName(principal);
        if ((generatedBy == null || generatedBy.trim().isEmpty())
                && request.getGeneratedBy() != null
                && !request.getGeneratedBy().trim().isEmpty()) {
            generatedBy = request.getGeneratedBy();
        }
        if (generatedBy == null || generatedBy.trim().isEmpty()) {
            generatedBy = "system";
        }

        return financialReportService.generateReport(organizationId, request, generatedBy);
    }

    @GetMapping("/financial-reports/org/{organizationId}")
    public List<FinancialReportDTO> listFinancialReports(
            @PathVariable Long organizationId,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return financialReportService.getReports(organizationId, reportType, fromDate, toDate);
    }

    @GetMapping("/financial-reports/{reportId}")
    public FinancialReportSummaryResponse getFinancialReport(@PathVariable Long reportId) {
        return financialReportService.getReportSummary(reportId);
    }

    @GetMapping("/trial-balance/org/{organizationId}")
    public TrialBalanceReportResponse generateTrialBalance(
            @PathVariable Long organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOfDate,
            Principal principal) {
        String generatedBy = getUserName(principal);
        if (generatedBy == null || generatedBy.trim().isEmpty()) {
            generatedBy = "system";
        }
        return trialBalanceService.generateTrialBalance(
                organizationId,
                asOfDate != null ? asOfDate : LocalDate.now(),
                generatedBy);
    }

    @PostMapping("/trial-balance/org/{organizationId}/snapshots")
    public TrialBalanceReportResponse saveTrialBalanceSnapshot(
            @PathVariable Long organizationId,
            @Valid @RequestBody(required = false) TrialBalanceGenerateRequest request,
            Principal principal) {
        TrialBalanceGenerateRequest snapshotRequest = request != null ? request : new TrialBalanceGenerateRequest();
        String generatedBy = getUserName(principal);
        if ((generatedBy == null || generatedBy.trim().isEmpty())
                && snapshotRequest.getGeneratedBy() != null
                && !snapshotRequest.getGeneratedBy().trim().isEmpty()) {
            generatedBy = snapshotRequest.getGeneratedBy();
        }
        if (generatedBy == null || generatedBy.trim().isEmpty()) {
            generatedBy = "system";
        }
        LocalDate asOfDate = snapshotRequest.getAsOfDate() != null ? snapshotRequest.getAsOfDate() : LocalDate.now();
        return trialBalanceService.saveTrialBalanceSnapshot(organizationId, asOfDate, generatedBy);
    }

    @GetMapping("/trial-balance/org/{organizationId}/snapshots")
    public List<TrialBalanceDTO> getTrialBalanceSnapshots(
            @PathVariable Long organizationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate reportDate) {
        return trialBalanceService.getSnapshotsByReportDate(organizationId, reportDate);
    }

    @PatchMapping("/financial-reports/{reportId}/approve")
    public FinancialReportDTO approveFinancialReport(
            @PathVariable Long reportId,
            @Valid @RequestBody(required = false) FinancialReportApprovalRequest request,
            Principal principal) {
        String approverName = getUserName(principal);
        if ((approverName == null || approverName.trim().isEmpty())
                && request != null
                && request.getApprovedBy() != null
                && !request.getApprovedBy().trim().isEmpty()) {
            approverName = request.getApprovedBy();
        }
        if (approverName == null || approverName.trim().isEmpty()) {
            approverName = "system";
        }
        return financialReportService.approveReport(reportId, approverName);
    }

    @PatchMapping("/manual-journals/{journalId}/reject")
    public ManualJournalDTO rejectManualJournal(
            @PathVariable Long journalId,
            @Valid @RequestBody JournalRejectionRequest request,
            Principal principal) {
        String rejecterName = getUserName(principal);
        if ((rejecterName == null || rejecterName.trim().isEmpty())
                && request.getRejectedBy() != null
                && !request.getRejectedBy().trim().isEmpty()) {
            rejecterName = request.getRejectedBy();
        }
        return manualJournalService.rejectJournal(journalId, request, rejecterName);
    }

    private PendingJournalApprovalResponse toPendingJournalApprovalResponse(ManualJournalDTO journal) {
        return PendingJournalApprovalResponse.builder()
                .id(journal.getId())
                .journalId(journal.getId())
                .journalDate(journal.getJournalDate())
                .description(journal.getDescription())
                .createdBy(journal.getCreatedBy())
                .submittedAt(journal.getSubmittedAt())
                .branchId(journal.getBranchId())
                .totalDebits(journal.getTotalDebits())
                .totalCredits(journal.getTotalCredits())
                .status(journal.getStatus())
                .build();
    }

    private String getUserName(Principal principal) {
        return principal != null ? principal.getName() : null;
    }
}




