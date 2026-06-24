package com.justjava.ams.accountant.service;

import com.justjava.ams.accountant.dto.GeneralLedgerDTO;
import com.justjava.ams.accountant.entity.ChartOfAccounts;
import com.justjava.ams.accountant.entity.GeneralLedger;
import com.justjava.ams.accountant.entity.JournalLine;
import com.justjava.ams.accountant.entity.ManualJournal;
import com.justjava.ams.accountant.repository.ChartOfAccountsRepository;
import com.justjava.ams.accountant.repository.GeneralLedgerRepository;
import com.justjava.ams.accountant.repository.JournalLineRepository;
import com.justjava.ams.accountant.repository.ManualJournalRepository;
import com.justjava.ams.auditor.dto.AuditLogDTO;
import com.justjava.ams.auditor.service.AuditLogService;
import com.justjava.ams.common.entity.User;
import com.justjava.ams.common.repository.UserRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GeneralLedgerService {

    private final GeneralLedgerRepository generalLedgerRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final UserRepository userRepository;
    private final ManualJournalRepository manualJournalRepository;
    private final JournalLineRepository journalLineRepository;
    private final AuditLogService auditLogService;

    public GeneralLedgerDTO createEntry(GeneralLedgerDTO dto, Long userId) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GeneralLedger entry = GeneralLedger.builder()
                .account(account)
                .journalNumber(dto.getJournalNumber())
                .transactionDate(dto.getTransactionDate())
                .debitCredit(GeneralLedger.DebitCredit.valueOf(dto.getDebitCredit()))
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .referenceNumber(dto.getReferenceNumber())
                .createdByUser(user)
                .notes(dto.getNotes())
                .status(GeneralLedger.TransactionStatus.PENDING)
                .build();

        GeneralLedger saved = generalLedgerRepository.save(entry);
        return mapToDTO(saved);
    }

    public List<GeneralLedgerDTO> postJournalEntriesFromManualJournal(Long journalId, String postedBy) {
        ManualJournal journal = manualJournalRepository.findById(journalId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Journal not found"));

        if (!ManualJournal.JournalStatus.APPROVED.equals(journal.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only APPROVED journals can be posted to GL");
        }

        User postingUser = null;
        if (postedBy != null) {
            postingUser = userRepository.findByUsername(postedBy).orElse(null);
        }

        String journalNumber = "MJ-" + journal.getId();
        List<GeneralLedger> existing = generalLedgerRepository.findByJournalNumber(journalNumber);
        if (existing != null && !existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "General ledger entries already exist for this journal");
        }

        List<JournalLine> lines = journalLineRepository.findByManualJournalId(journalId);
        if (lines.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Journal must have lines before posting");
        }

        List<GeneralLedger> postedEntries = new java.util.ArrayList<>();
        for (JournalLine line : lines) {
            if (line.getDebitAmount() != null && line.getDebitAmount().compareTo(BigDecimal.ZERO) > 0) {
                GeneralLedger debit = GeneralLedger.builder()
                        .account(line.getChartOfAccounts())
                        .journalNumber(journalNumber)
                        .transactionDate(journal.getJournalDate())
                        .debitCredit(GeneralLedger.DebitCredit.DEBIT)
                        .amount(line.getDebitAmount())
                        .description(journal.getDescription() != null ? journal.getDescription() : line.getNarration())
                        .referenceNumber(null)
                        .createdByUser(postingUser)
                        .notes(line.getNarration())
                        .status(GeneralLedger.TransactionStatus.POSTED)
                        .build();
                postedEntries.add(generalLedgerRepository.save(debit));
            }

            if (line.getCreditAmount() != null && line.getCreditAmount().compareTo(BigDecimal.ZERO) > 0) {
                GeneralLedger credit = GeneralLedger.builder()
                        .account(line.getChartOfAccounts())
                        .journalNumber(journalNumber)
                        .transactionDate(journal.getJournalDate())
                        .debitCredit(GeneralLedger.DebitCredit.CREDIT)
                        .amount(line.getCreditAmount())
                        .description(journal.getDescription() != null ? journal.getDescription() : line.getNarration())
                        .referenceNumber(null)
                        .createdByUser(postingUser)
                        .notes(line.getNarration())
                        .status(GeneralLedger.TransactionStatus.POSTED)
                        .build();
                postedEntries.add(generalLedgerRepository.save(credit));
            }
        }

        try {
            AuditLogDTO log = AuditLogDTO.builder()
                    .organizationId(journal.getOrganization().getId())
                    .entityType("GeneralLedger")
                    .entityId(journal.getId())
                    .action("POST")
                    .newValue(journalNumber)
                    .description("Posted " + postedEntries.size() + " general ledger entries for ManualJournal " + journal.getId())
                    .build();
            auditLogService.createAuditLog(journal.getOrganization().getId(), log);
        } catch (Exception ex) {
        }

        return postedEntries.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public GeneralLedgerDTO getEntry(Long entryId) {
        GeneralLedger entry = generalLedgerRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        return mapToDTO(entry);
    }

    public List<GeneralLedgerDTO> getEntriesByAccount(Long accountId) {
        return generalLedgerRepository.findByAccountIdOrderByTransactionDateDesc(accountId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GeneralLedgerDTO> getEntriesByJournalId(Long journalId) {
        return generalLedgerRepository.findByJournalNumber("MJ-" + journalId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<GeneralLedgerDTO> getEntriesByDateRange(LocalDate startDate, LocalDate endDate) {
        return generalLedgerRepository.findByTransactionDateBetween(startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public GeneralLedgerDTO approveEntry(Long entryId) {
        GeneralLedger entry = generalLedgerRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));
        entry.setStatus(GeneralLedger.TransactionStatus.APPROVED);
        GeneralLedger updated = generalLedgerRepository.save(entry);
        return mapToDTO(updated);
    }

    private GeneralLedgerDTO mapToDTO(GeneralLedger entry) {
        return GeneralLedgerDTO.builder()
                .id(entry.getId())
                .accountId(entry.getAccount().getId())
                .journalNumber(entry.getJournalNumber())
                .transactionDate(entry.getTransactionDate())
                .debitCredit(entry.getDebitCredit().toString())
                .amount(entry.getAmount())
                .description(entry.getDescription())
                .referenceNumber(entry.getReferenceNumber())
                .createdByUserId(entry.getCreatedByUser() != null ? entry.getCreatedByUser().getId() : null)
                .notes(entry.getNotes())
                .status(entry.getStatus().toString())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}

