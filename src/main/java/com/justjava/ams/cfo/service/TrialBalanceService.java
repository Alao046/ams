package com.justjava.ams.cfo.service;

import com.justjava.ams.accountant.entity.ChartOfAccounts;
import com.justjava.ams.accountant.entity.GeneralLedger;
import com.justjava.ams.accountant.repository.GeneralLedgerRepository;
import com.justjava.ams.auditor.service.AuditLogService;
import com.justjava.ams.auditor.service.SecurityEventService;
import com.justjava.ams.cfo.dto.TrialBalanceDTO;
import com.justjava.ams.cfo.dto.TrialBalanceLineDTO;
import com.justjava.ams.cfo.dto.TrialBalanceReportResponse;
import com.justjava.ams.cfo.entity.TrialBalance;
import com.justjava.ams.cfo.repository.TrialBalanceRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.repository.OrganizationRepository;
import com.justjava.ams.financeAdmin.entity.ModuleControl;
import com.justjava.ams.financeAdmin.service.ModuleControlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TrialBalanceService {

    private final TrialBalanceRepository trialBalanceRepository;
    private final OrganizationRepository organizationRepository;
    private final GeneralLedgerRepository generalLedgerRepository;
    private final AuditLogService auditLogService;
    private final SecurityEventService securityEventService;
    private final ModuleControlService moduleControlService;

    public TrialBalanceDTO createTrialBalance(Long organizationId, TrialBalanceDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Organization not found"));

        TrialBalance trialBalance = TrialBalance.builder()
                .organization(organization)
                .reportDate(dto.getReportDate())
                .accountCode(dto.getAccountCode())
                .accountName(dto.getAccountName())
                .debitBalance(dto.getDebitBalance())
                .creditBalance(dto.getCreditBalance())
                .status(dto.getStatus() != null ? TrialBalance.ReportStatus.valueOf(dto.getStatus()) : TrialBalance.ReportStatus.OPEN)
                .notes(dto.getNotes())
                .build();

        return mapToDTO(trialBalanceRepository.save(trialBalance));
    }

    public TrialBalanceDTO getTrialBalance(Long trialBalanceId) {
        TrialBalance trialBalance = trialBalanceRepository.findById(trialBalanceId)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Trial balance not found"));
        return mapToDTO(trialBalance);
    }

    public List<TrialBalanceDTO> getByReportDate(Long organizationId, LocalDate reportDate) {
        return trialBalanceRepository.findByOrganizationIdAndReportDate(organizationId, reportDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TrialBalanceDTO> getByStatus(Long organizationId, String status) {
        try {
            TrialBalance.ReportStatus rs = TrialBalance.ReportStatus.valueOf(status);
            return trialBalanceRepository.findByOrganizationIdAndStatus(organizationId, rs)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid trial balance status");
        }
    }

    @Transactional(readOnly = true)
    public TrialBalanceReportResponse generateTrialBalance(Long organizationId, LocalDate asOfDate, String generatedBy) {
        findOrganization(organizationId);
        if (asOfDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "As of date is required");
        }
        requireModuleIfConfigured(organizationId, ModuleControl.ModuleType.GENERAL_LEDGER);
        requireModuleIfConfigured(organizationId, ModuleControl.ModuleType.REPORTING);

        List<GeneralLedger> entries = generalLedgerRepository.findPostedEntriesByOrganizationAsOf(organizationId, asOfDate);
        Map<Long, List<GeneralLedger>> groupedByAccount = entries.stream()
                .collect(Collectors.groupingBy(gl -> gl.getAccount().getId()));

        List<TrialBalanceLineDTO> lines = new ArrayList<>();
        for (List<GeneralLedger> accountEntries : groupedByAccount.values()) {
            ChartOfAccounts account = accountEntries.get(0).getAccount();
            BigDecimal debitTotal = sum(accountEntries, GeneralLedger.DebitCredit.DEBIT);
            BigDecimal creditTotal = sum(accountEntries, GeneralLedger.DebitCredit.CREDIT);
            BigDecimal net = debitTotal.subtract(creditTotal);
            BigDecimal debitBalance = BigDecimal.ZERO;
            BigDecimal creditBalance = BigDecimal.ZERO;

            if (ChartOfAccounts.DebitCredit.DEBIT.equals(account.getNormalBalance())) {
                if (net.compareTo(BigDecimal.ZERO) >= 0) {
                    debitBalance = net;
                } else {
                    creditBalance = net.abs();
                }
            } else {
                BigDecimal creditNet = creditTotal.subtract(debitTotal);
                if (creditNet.compareTo(BigDecimal.ZERO) >= 0) {
                    creditBalance = creditNet;
                } else {
                    debitBalance = creditNet.abs();
                }
            }

            lines.add(TrialBalanceLineDTO.builder()
                    .accountId(account.getId())
                    .accountCode(account.getAccountCode())
                    .accountName(account.getAccountName())
                    .accountType(account.getAccountType().name())
                    .normalBalance(account.getNormalBalance().name())
                    .debitBalance(debitBalance)
                    .creditBalance(creditBalance)
                    .build());
        }

        lines.sort(Comparator.comparing(TrialBalanceLineDTO::getAccountCode));
        BigDecimal totalDebits = lines.stream().map(TrialBalanceLineDTO::getDebitBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredits = lines.stream().map(TrialBalanceLineDTO::getCreditBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal variance = totalDebits.subtract(totalCredits);
        boolean balanced = variance.compareTo(BigDecimal.ZERO) == 0;

        return TrialBalanceReportResponse.builder()
                .organizationId(organizationId)
                .asOfDate(asOfDate)
                .lines(lines)
                .totalDebits(totalDebits)
                .totalCredits(totalCredits)
                .variance(variance)
                .balanced(balanced)
                .status(balanced ? "BALANCED" : "UNBALANCED")
                .generatedAt(LocalDateTime.now())
                .build();
    }

    public TrialBalanceReportResponse saveTrialBalanceSnapshot(Long organizationId, LocalDate asOfDate, String generatedBy) {
        Organization organization = findOrganization(organizationId);
        TrialBalanceReportResponse response = generateTrialBalance(organizationId, asOfDate, generatedBy);
        TrialBalance.ReportStatus status = Boolean.TRUE.equals(response.getBalanced())
                ? TrialBalance.ReportStatus.BALANCED
                : TrialBalance.ReportStatus.UNBALANCED;

        trialBalanceRepository.deleteByOrganizationIdAndReportDate(organizationId, asOfDate);
        Long auditEntityId = null;
        for (TrialBalanceLineDTO line : response.getLines()) {
            TrialBalance saved = trialBalanceRepository.save(TrialBalance.builder()
                    .organization(organization)
                    .reportDate(asOfDate)
                    .accountCode(line.getAccountCode())
                    .accountName(line.getAccountName())
                    .debitBalance(line.getDebitBalance())
                    .creditBalance(line.getCreditBalance())
                    .status(status)
                    .notes("Generated by " + defaultUser(generatedBy))
                    .build());
            if (auditEntityId == null) {
                auditEntityId = saved.getId();
            }
        }

        auditLogService.log(organizationId, "TrialBalance", auditEntityId != null ? auditEntityId : organizationId, "CREATE",
                null, response.getStatus(), "Trial balance snapshot saved as of " + asOfDate);
        securityEventService.logEvent(organizationId, "REPORT_GENERATION", "LOW",
                "Trial Balance Snapshot Saved",
                "Trial balance snapshot saved as of " + asOfDate,
                null, null);
        return response;
    }

    @Transactional(readOnly = true)
    public List<TrialBalanceDTO> getSnapshotsByReportDate(Long organizationId, LocalDate reportDate) {
        findOrganization(organizationId);
        if (reportDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Report date is required");
        }
        return trialBalanceRepository.findByOrganizationIdAndReportDateOrderByAccountCodeAsc(organizationId, reportDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BigDecimal sum(List<GeneralLedger> entries, GeneralLedger.DebitCredit debitCredit) {
        return entries.stream()
                .filter(gl -> debitCredit.equals(gl.getDebitCredit()))
                .map(GeneralLedger::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Organization findOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
    }

    private void requireModuleIfConfigured(Long organizationId, ModuleControl.ModuleType moduleType) {
        try {
            moduleControlService.requireModuleEnabled(organizationId, moduleType);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                return;
            }
            throw ex;
        }
    }

    private String defaultUser(String generatedBy) {
        return generatedBy != null && !generatedBy.trim().isEmpty() ? generatedBy : "system";
    }

    private TrialBalanceDTO mapToDTO(TrialBalance trialBalance) {
        return TrialBalanceDTO.builder()
                .id(trialBalance.getId())
                .organizationId(trialBalance.getOrganization().getId())
                .reportDate(trialBalance.getReportDate())
                .accountCode(trialBalance.getAccountCode())
                .accountName(trialBalance.getAccountName())
                .debitBalance(trialBalance.getDebitBalance())
                .creditBalance(trialBalance.getCreditBalance())
                .status(trialBalance.getStatus() != null ? trialBalance.getStatus().toString() : TrialBalance.ReportStatus.OPEN.toString())
                .notes(trialBalance.getNotes())
                .createdAt(trialBalance.getCreatedAt())
                .updatedAt(trialBalance.getUpdatedAt())
                .build();
    }
}
