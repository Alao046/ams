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
import com.justjava.ams.common.entity.User;
import com.justjava.ams.common.repository.UserRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public void postJournalEntriesFromManualJournal(Long journalId, String approverName) {
        ManualJournal journal = manualJournalRepository.findById(journalId)
                .orElseThrow(() -> new RuntimeException("Journal not found"));

        if (!journal.getStatus().equals(ManualJournal.JournalStatus.APPROVED)) {
            throw new RuntimeException("Only APPROVED journals can be posted to GL");
        }

        User approver = null;
        if (approverName != null) {
            approver = userRepository.findByUsername(approverName).orElse(null);
        }

        String journalNumber = "MJ-" + journal.getId();

        // idempotency: if GL entries for this journalNumber already posted, skip
        java.util.List<GeneralLedger> existing = generalLedgerRepository.findByJournalNumberAndStatus(journalNumber, GeneralLedger.TransactionStatus.POSTED);
        if (existing != null && !existing.isEmpty()) {
            return; // already posted
        }

        // create GL entries for each journal line
        java.util.List<JournalLine> lines = journalLineRepository.findByManualJournalId(journalId);
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
                        .createdByUser(approver)
                        .notes(line.getNarration())
                        .status(GeneralLedger.TransactionStatus.POSTED)
                        .build();
                generalLedgerRepository.save(debit);
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
                        .createdByUser(approver)
                        .notes(line.getNarration())
                        .status(GeneralLedger.TransactionStatus.POSTED)
                        .build();
                generalLedgerRepository.save(credit);
            }
        }
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

