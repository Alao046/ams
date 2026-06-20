package com.justjava.ams.accountant.service;

import com.justjava.ams.accountant.dto.JournalLineDTO;
import com.justjava.ams.accountant.dto.ManualJournalDTO;
import com.justjava.ams.accountant.entity.ChartOfAccounts;
import com.justjava.ams.accountant.entity.JournalLine;
import com.justjava.ams.accountant.entity.ManualJournal;
import com.justjava.ams.accountant.repository.ChartOfAccountsRepository;
import com.justjava.ams.accountant.repository.ManualJournalRepository;
import com.justjava.ams.accountant.repository.JournalLineRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.repository.OrganizationRepository;
import com.justjava.ams.auditor.dto.AuditLogDTO;
import com.justjava.ams.auditor.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ManualJournalService {

    private final ManualJournalRepository manualJournalRepository;
    private final JournalLineRepository journalLineRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditLogService auditLogService;

    public ManualJournalDTO createManualJournal(Long organizationId, ManualJournalDTO dto, String userName) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        ManualJournal journal = ManualJournal.builder()
                .organization(organization)
                .description(dto.getDescription())
                .journalDate(dto.getJournalDate())
                .status(ManualJournal.JournalStatus.DRAFT)
                .createdBy(userName)
                .build();

        ManualJournal saved = manualJournalRepository.save(journal);
        // Audit: record creation of manual journal
        try {
            AuditLogDTO log = AuditLogDTO.builder()
                    .organizationId(saved.getOrganization().getId())
                    .entityType("ManualJournal")
                    .entityId(saved.getId())
                    .action("CREATE")
                    .newValue(saved.getStatus().toString())
                    .description("Manual journal created by " + userName)
                    .build();
            auditLogService.createAuditLog(saved.getOrganization().getId(), log);
        } catch (Exception ex) {
            // Swallow audit errors to avoid breaking main flow
        }
        return mapToDTO(saved);
    }

    public JournalLineDTO addJournalLine(Long journalId, JournalLineDTO dto) {
        ManualJournal journal = manualJournalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        if (!journal.getStatus().equals(ManualJournal.JournalStatus.DRAFT)) {
            throw new RuntimeException("Can only add lines to DRAFT journals");
        }

        ChartOfAccounts account = chartOfAccountsRepository.findById(dto.getChartOfAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        JournalLine line = JournalLine.builder()
                .manualJournal(journal)
                .chartOfAccounts(account)
                .debitAmount(dto.getDebitAmount() != null ? dto.getDebitAmount() : BigDecimal.ZERO)
                .creditAmount(dto.getCreditAmount() != null ? dto.getCreditAmount() : BigDecimal.ZERO)
                .departmentCode(dto.getDepartmentCode())
                .projectCode(dto.getProjectCode())
                .branchCode(dto.getBranchCode())
                .narration(dto.getNarration())
                .lineSequence(dto.getLineSequence())
                .build();

        JournalLine saved = journalLineRepository.save(line);
        // Audit: record addition of a journal line
        try {
            AuditLogDTO log = AuditLogDTO.builder()
                    .organizationId(journal.getOrganization().getId())
                    .entityType("JournalLine")
                    .entityId(saved.getId())
                    .action("CREATE")
                    .newValue(saved.getNarration())
                    .description("Added line to ManualJournal " + journal.getId())
                    .build();
            auditLogService.createAuditLog(journal.getOrganization().getId(), log);
        } catch (Exception ex) {
        }
        return mapLineToDTO(saved);
    }

    public JournalLineDTO updateJournalLine(Long lineId, JournalLineDTO dto) {
        JournalLine line = journalLineRepository.findById(lineId)
                .orElseThrow(() -> new RuntimeException("Journal line not found"));

        if (!line.getManualJournal().getStatus().equals(ManualJournal.JournalStatus.DRAFT)) {
            throw new RuntimeException("Can only edit lines in DRAFT journals");
        }

        line.setDebitAmount(dto.getDebitAmount() != null ? dto.getDebitAmount() : BigDecimal.ZERO);
        line.setCreditAmount(dto.getCreditAmount() != null ? dto.getCreditAmount() : BigDecimal.ZERO);
        line.setDepartmentCode(dto.getDepartmentCode());
        line.setProjectCode(dto.getProjectCode());
        line.setBranchCode(dto.getBranchCode());
        line.setNarration(dto.getNarration());

        JournalLine saved = journalLineRepository.save(line);
        // Audit: record update of a journal line
        try {
            AuditLogDTO log = AuditLogDTO.builder()
                    .organizationId(line.getManualJournal().getOrganization().getId())
                    .entityType("JournalLine")
                    .entityId(saved.getId())
                    .action("UPDATE")
                    .newValue(saved.getNarration())
                    .description("Updated line " + saved.getId() + " in ManualJournal " + saved.getManualJournal().getId())
                    .build();
            auditLogService.createAuditLog(saved.getManualJournal().getOrganization().getId(), log);
        } catch (Exception ex) {
        }
        return mapLineToDTO(saved);
    }

    public void deleteJournalLine(Long lineId) {
        JournalLine line = journalLineRepository.findById(lineId)
                .orElseThrow(() -> new RuntimeException("Journal line not found"));

        if (!line.getManualJournal().getStatus().equals(ManualJournal.JournalStatus.DRAFT)) {
            throw new RuntimeException("Can only delete lines from DRAFT journals");
        }

        journalLineRepository.deleteById(lineId);
        // Audit: record deletion of a journal line
        try {
            AuditLogDTO log = AuditLogDTO.builder()
                    .organizationId(line.getManualJournal().getOrganization().getId())
                    .entityType("JournalLine")
                    .entityId(line.getId())
                    .action("DELETE")
                    .description("Deleted line " + line.getId() + " from ManualJournal " + line.getManualJournal().getId())
                    .build();
            auditLogService.createAuditLog(line.getManualJournal().getOrganization().getId(), log);
        } catch (Exception ex) {
        }
    }

    public ManualJournalDTO submitForApproval(Long journalId, String userName) {
        ManualJournal journal = manualJournalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        if (!journal.getStatus().equals(ManualJournal.JournalStatus.DRAFT)) {
            throw new RuntimeException("Only DRAFT journals can be submitted");
        }

        if (!journal.isBalanced()) {
            throw new RuntimeException("Journal must be balanced (debits = credits) before submission");
        }

        journal.setStatus(ManualJournal.JournalStatus.SUBMITTED);
        journal.setSubmittedBy(userName);
        journal.setSubmittedAt(LocalDateTime.now());

        ManualJournal saved = manualJournalRepository.save(journal);
        // Audit: record submission
        try {
            AuditLogDTO log = AuditLogDTO.builder()
                    .organizationId(saved.getOrganization().getId())
                    .entityType("ManualJournal")
                    .entityId(saved.getId())
                    .action("UPDATE")
                    .newValue(saved.getStatus().toString())
                    .description("Submitted for approval by " + userName)
                    .build();
            auditLogService.createAuditLog(saved.getOrganization().getId(), log);
        } catch (Exception ex) {
        }
        return mapToDTO(saved);
    }

    public ManualJournalDTO approveJournal(Long journalId, String approverName) {
        ManualJournal journal = manualJournalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        if (!journal.getStatus().equals(ManualJournal.JournalStatus.SUBMITTED)) {
            throw new RuntimeException("Only SUBMITTED journals can be approved");
        }

        journal.setStatus(ManualJournal.JournalStatus.APPROVED);
        journal.setApprovedBy(approverName);
        journal.setApprovedAt(LocalDateTime.now());

        ManualJournal saved = manualJournalRepository.save(journal);
        // Audit: record approval
        try {
            AuditLogDTO log = AuditLogDTO.builder()
                    .organizationId(saved.getOrganization().getId())
                    .entityType("ManualJournal")
                    .entityId(saved.getId())
                    .action("APPROVE")
                    .newValue(saved.getStatus().toString())
                    .description("Approved by " + approverName)
                    .build();
            auditLogService.createAuditLog(saved.getOrganization().getId(), log);
        } catch (Exception ex) {
        }
        return mapToDTO(saved);
    }

    public ManualJournalDTO rejectJournal(Long journalId, String rejectionReason, String rejecterName) {
        ManualJournal journal = manualJournalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        if (!journal.getStatus().equals(ManualJournal.JournalStatus.SUBMITTED)) {
            throw new RuntimeException("Only SUBMITTED journals can be rejected");
        }

        journal.setStatus(ManualJournal.JournalStatus.REJECTED);
        journal.setRejectionReason(rejectionReason);

        ManualJournal saved = manualJournalRepository.save(journal);
        // Audit: record rejection
        try {
            AuditLogDTO log = AuditLogDTO.builder()
                    .organizationId(saved.getOrganization().getId())
                    .entityType("ManualJournal")
                    .entityId(saved.getId())
                    .action("REJECT")
                    .newValue(saved.getStatus().toString())
                    .description("Rejected: " + rejectionReason)
                    .build();
            auditLogService.createAuditLog(saved.getOrganization().getId(), log);
        } catch (Exception ex) {
        }
        return mapToDTO(saved);
    }

    public ManualJournalDTO postJournal(Long journalId) {
        ManualJournal journal = manualJournalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        if (!journal.getStatus().equals(ManualJournal.JournalStatus.APPROVED)) {
            throw new RuntimeException("Only APPROVED journals can be posted");
        }

        journal.setStatus(ManualJournal.JournalStatus.POSTED);
        journal.setPostingDate(java.time.LocalDate.now());

        ManualJournal saved = manualJournalRepository.save(journal);
        // Audit: record posting
        try {
            AuditLogDTO log = AuditLogDTO.builder()
                    .organizationId(saved.getOrganization().getId())
                    .entityType("ManualJournal")
                    .entityId(saved.getId())
                    .action("UPDATE")
                    .newValue(saved.getStatus().toString())
                    .description("Posted to GL")
                    .build();
            auditLogService.createAuditLog(saved.getOrganization().getId(), log);
        } catch (Exception ex) {
        }
        return mapToDTO(saved);
    }

    public ManualJournalDTO getJournal(Long journalId) {
        ManualJournal journal = manualJournalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));
        return mapToDTO(journal);
    }

    public List<ManualJournalDTO> getJournalsByOrganization(Long organizationId) {
        return manualJournalRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ManualJournalDTO> getJournalsByStatus(Long organizationId, String status) {
        ManualJournal.JournalStatus journalStatus = ManualJournal.JournalStatus.valueOf(status);
        return manualJournalRepository.findByOrganizationIdAndStatus(organizationId, journalStatus)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ManualJournalDTO> getPendingJournals(Long organizationId) {
        return getJournalsByStatus(organizationId, "SUBMITTED");
    }

    public List<JournalLineDTO> getJournalLines(Long journalId) {
        return journalLineRepository.findByManualJournalId(journalId)
                .stream()
                .map(this::mapLineToDTO)
                .collect(Collectors.toList());
    }

    private ManualJournalDTO mapToDTO(ManualJournal journal) {
        List<JournalLineDTO> lines = journalLineRepository.findByManualJournalId(journal.getId())
                .stream()
                .map(this::mapLineToDTO)
                .collect(Collectors.toList());

        return ManualJournalDTO.builder()
                .id(journal.getId())
                .organizationId(journal.getOrganization().getId())
                .branchId(journal.getBranch() != null ? journal.getBranch().getId() : null)
                .description(journal.getDescription())
                .journalDate(journal.getJournalDate())
                .postingDate(journal.getPostingDate())
                .status(journal.getStatus().toString())
                .createdBy(journal.getCreatedBy())
                .submittedBy(journal.getSubmittedBy())
                .approvedBy(journal.getApprovedBy())
                .rejectionReason(journal.getRejectionReason())
                .createdAt(journal.getCreatedAt())
                .submittedAt(journal.getSubmittedAt())
                .approvedAt(journal.getApprovedAt())
                .updatedAt(journal.getUpdatedAt())
                .journalLines(lines)
                .totalDebits(journal.getTotalDebits())
                .totalCredits(journal.getTotalCredits())
                .balanced(journal.isBalanced())
                .build();
    }

    private JournalLineDTO mapLineToDTO(JournalLine line) {
        return JournalLineDTO.builder()
                .id(line.getId())
                .manualJournalId(line.getManualJournal().getId())
                .chartOfAccountId(line.getChartOfAccounts().getId())
                .accountCode(line.getChartOfAccounts().getAccountCode())
                .accountName(line.getChartOfAccounts().getAccountName())
                .debitAmount(line.getDebitAmount())
                .creditAmount(line.getCreditAmount())
                .departmentCode(line.getDepartmentCode())
                .projectCode(line.getProjectCode())
                .branchCode(line.getBranchCode())
                .narration(line.getNarration())
                .lineSequence(line.getLineSequence())
                .createdAt(line.getCreatedAt())
                .build();
    }
}
