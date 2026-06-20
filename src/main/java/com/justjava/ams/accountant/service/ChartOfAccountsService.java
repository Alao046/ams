package com.justjava.ams.accountant.service;

import com.justjava.ams.accountant.dto.ChartOfAccountsDTO;
import com.justjava.ams.accountant.entity.ChartOfAccounts;
import com.justjava.ams.accountant.repository.ChartOfAccountsRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChartOfAccountsService {

    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final OrganizationRepository organizationRepository;

    public ChartOfAccountsDTO createAccount(Long organizationId, ChartOfAccountsDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        ChartOfAccounts account = ChartOfAccounts.builder()
                .organization(organization)
                .accountCode(dto.getAccountCode())
                .accountName(dto.getAccountName())
                .description(dto.getDescription())
                .accountType(ChartOfAccounts.AccountType.valueOf(dto.getAccountType()))
                .accountSubtype(ChartOfAccounts.AccountSubtype.valueOf(dto.getAccountSubtype()))
                .normalBalance(ChartOfAccounts.DebitCredit.valueOf(dto.getNormalBalance()))
                .active(true)
                .build();

        ChartOfAccounts saved = chartOfAccountsRepository.save(account);
        return mapToDTO(saved);
    }

    public ChartOfAccountsDTO getAccount(Long accountId) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return mapToDTO(account);
    }

    public List<ChartOfAccountsDTO> getAccountsByOrganization(Long organizationId) {
        return chartOfAccountsRepository.findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ChartOfAccountsDTO> getAccountsByType(Long organizationId, String accountType) {
        return chartOfAccountsRepository.findByOrganizationIdAndAccountType(
                organizationId,
                ChartOfAccounts.AccountType.valueOf(accountType))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ChartOfAccountsDTO updateAccount(Long accountId, ChartOfAccountsDTO dto) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (dto.getAccountName() != null) account.setAccountName(dto.getAccountName());
        if (dto.getDescription() != null) account.setDescription(dto.getDescription());
        if (dto.getActive() != null) account.setActive(dto.getActive());

        ChartOfAccounts updated = chartOfAccountsRepository.save(account);
        return mapToDTO(updated);
    }

    public void deleteAccount(Long accountId) {
        ChartOfAccounts account = chartOfAccountsRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setActive(false);
        chartOfAccountsRepository.save(account);
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
}

