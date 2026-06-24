package com.justjava.ams.accountant.service;

import com.justjava.ams.auditor.dto.AuditLogDTO;
import com.justjava.ams.auditor.service.AuditLogService;
import com.justjava.ams.accountant.dto.ChartOfAccountsDTO;
import com.justjava.ams.accountant.entity.ChartOfAccounts;
import com.justjava.ams.accountant.repository.ChartOfAccountsRepository;
import com.justjava.ams.accountant.entity.GeneralLedger;
import com.justjava.ams.accountant.repository.GeneralLedgerRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChartOfAccountsService {
    private static final String ENTITY_TYPE = "ChartOfAccounts";

    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final OrganizationRepository organizationRepository;
    private final GeneralLedgerRepository generalLedgerRepository;
    private final AuditLogService auditLogService;

    public ChartOfAccountsDTO createAccount(Long organizationId, ChartOfAccountsDTO dto) {
        Organization organization = findOrganization(organizationId);
        String accountCode = normalizeRequired(dto.getAccountCode(), "Account code is required");
        String accountName = normalizeRequired(dto.getAccountName(), "Account name is required");

        if (chartOfAccountsRepository.existsByOrganizationIdAndAccountCodeIgnoreCase(organizationId, accountCode)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account code already exists for this organization");
        }

        ChartOfAccounts account = ChartOfAccounts.builder()
                .organization(organization)
                .accountCode(accountCode)
                .accountName(accountName)
                .description(trimToNull(dto.getDescription()))
                .accountType(parseAccountType(dto.getAccountType()))
                .accountSubtype(parseAccountSubtype(dto.getAccountSubtype()))
                .normalBalance(parseDebitCredit(dto.getNormalBalance()))
                .active(true)
                .build();

        ChartOfAccounts saved = chartOfAccountsRepository.save(account);
        ChartOfAccountsDTO savedDto = mapToDTO(saved);
        log(organizationId, saved.getId(), "CREATE", null, savedDto.toString(), "Created account " + saved.getAccountCode());
        return savedDto;
    }

    @Transactional(readOnly = true)
    public ChartOfAccountsDTO getAccount(Long accountId) {
        return mapToDTO(findAccount(accountId));
    }

    @Transactional(readOnly = true)
    public List<ChartOfAccountsDTO> getAccountsByOrganization(Long organizationId) {
        findOrganization(organizationId);

        return chartOfAccountsRepository.findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChartOfAccountsDTO> getAccountsByType(Long organizationId, String accountType) {
        findOrganization(organizationId);

        return chartOfAccountsRepository.findByOrganizationIdAndAccountType(
                organizationId,
                parseAccountType(accountType))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ChartOfAccountsDTO updateAccount(Long accountId, ChartOfAccountsDTO dto) {
        ChartOfAccounts account = findAccount(accountId);
        ChartOfAccountsDTO oldValue = mapToDTO(account);

        account.setAccountName(normalizeRequired(dto.getAccountName(), "Account name is required"));
        account.setDescription(trimToNull(dto.getDescription()));

        if (dto.getAccountType() != null || dto.getAccountSubtype() != null || dto.getNormalBalance() != null) {
            ensureNoPostedEntries(account.getId(), "Cannot change account classification after posted GL entries exist");
        }

        if (dto.getAccountType() != null) {
            account.setAccountType(parseAccountType(dto.getAccountType()));
        }
        if (dto.getAccountSubtype() != null) {
            account.setAccountSubtype(parseAccountSubtype(dto.getAccountSubtype()));
        }
        if (dto.getNormalBalance() != null) {
            account.setNormalBalance(parseDebitCredit(dto.getNormalBalance()));
        }
        if (dto.getActive() != null) {
            if (!dto.getActive()) {
                ensureNoPostedEntries(account.getId(), "Cannot deactivate an account with posted GL entries");
            }
            account.setActive(dto.getActive());
        }

        ChartOfAccounts updated = chartOfAccountsRepository.save(account);
        ChartOfAccountsDTO updatedDto = mapToDTO(updated);
        log(updated.getOrganization().getId(), updated.getId(), "UPDATE", oldValue.toString(), updatedDto.toString(), "Updated account " + updated.getAccountCode());
        return updatedDto;
    }

    public void deleteAccount(Long accountId) {
        ChartOfAccounts account = findAccount(accountId);
        ChartOfAccountsDTO oldValue = mapToDTO(account);

        ensureNoPostedEntries(accountId, "Cannot deactivate an account with posted GL entries");
        account.setActive(false);
        ChartOfAccounts saved = chartOfAccountsRepository.save(account);
        log(saved.getOrganization().getId(), saved.getId(), "DELETE", oldValue.toString(), mapToDTO(saved).toString(), "Deactivated account " + saved.getAccountCode());
    }

    private ChartOfAccountsDTO mapToDTO(ChartOfAccounts account) {
        return ChartOfAccountsDTO.builder()
                .id(account.getId())
                .organizationId(account.getOrganization().getId())
                .accountCode(account.getAccountCode())
                .accountName(account.getAccountName())
                .description(account.getDescription())
                .accountType(account.getAccountType().toString())
                .accountSubtype(account.getAccountSubtype().toString())
                .normalBalance(account.getNormalBalance().toString())
                .balance(account.getBalance())
                .active(account.getActive())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    private Organization findOrganization(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
    }

    private ChartOfAccounts findAccount(Long accountId) {
        return chartOfAccountsRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private void ensureNoPostedEntries(Long accountId, String message) {
        if (generalLedgerRepository.existsByAccountIdAndStatus(accountId, GeneralLedger.TransactionStatus.POSTED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, message);
        }
    }

    private ChartOfAccounts.AccountType parseAccountType(String value) {
        String normalized = normalizeRequired(value, "Account type is required").toUpperCase();
        if ("INCOME".equals(normalized)) {
            normalized = "REVENUE";
        }

        try {
            return ChartOfAccounts.AccountType.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account type");
        }
    }

    private ChartOfAccounts.AccountSubtype parseAccountSubtype(String value) {
        String normalized = normalizeRequired(value, "Account subtype is required").toUpperCase();

        try {
            return ChartOfAccounts.AccountSubtype.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid account subtype");
        }
    }

    private ChartOfAccounts.DebitCredit parseDebitCredit(String value) {
        String normalized = normalizeRequired(value, "Normal balance is required").toUpperCase();

        try {
            return ChartOfAccounts.DebitCredit.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid normal balance");
        }
    }

    private void log(Long organizationId, Long entityId, String action, String oldValue, String newValue, String description) {
        AuditLogDTO auditLog = AuditLogDTO.builder()
                .entityType(ENTITY_TYPE)
                .entityId(entityId)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .build();

        auditLogService.createAuditLog(organizationId, auditLog);
    }

    private String normalizeRequired(String value, String message) {
        String normalized = trimToNull(value);
        if (normalized == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
        return normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}

