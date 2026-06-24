package com.justjava.ams.cfo;

import com.justjava.ams.accountant.dto.ManualJournalDTO;
import com.justjava.ams.accountant.service.ManualJournalService;
import com.justjava.ams.cfo.dto.JournalApprovalRequest;
import com.justjava.ams.cfo.dto.JournalRejectionRequest;
import com.justjava.ams.cfo.dto.PendingJournalApprovalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cfo")
@RequiredArgsConstructor
public class CfoApiController {
    private final ManualJournalService manualJournalService;

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
