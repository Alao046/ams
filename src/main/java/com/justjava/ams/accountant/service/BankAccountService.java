package com.justjava.ams.accountant.service;

import com.justjava.ams.accountant.dto.BankAccountDTO;
import com.justjava.ams.accountant.entity.BankAccount;
import com.justjava.ams.accountant.entity.ChartOfAccounts;
import com.justjava.ams.accountant.repository.BankAccountRepository;
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
public class BankAccountService {

    private final BankAccountRepository bankAccountRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final OrganizationRepository organizationRepository;

    public BankAccountDTO createBankAccount(Long organizationId, BankAccountDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        ChartOfAccounts account = chartOfAccountsRepository.findById(dto.getChartAccountId())
                .orElseThrow(() -> new RuntimeException("Chart account not found"));

        BankAccount bankAccount = BankAccount.builder()
                .organization(organization)
                .chartAccount(account)
                .bankName(dto.getBankName())
                .accountNumber(dto.getAccountNumber())
                .accountHolder(dto.getAccountHolder())
                .branchCode(dto.getBranchCode())
                .routingNumber(dto.getRoutingNumber())
                .swiftCode(dto.getSwiftCode())
                .ibanCode(dto.getIbanCode())
                .currency(dto.getCurrency() != null ? dto.getCurrency() : "USD")
                .balance(dto.getBalance())
                .active(true)
                .notes(dto.getNotes())
                .build();

        return mapToDTO(bankAccountRepository.save(bankAccount));
    }

    public BankAccountDTO getBankAccount(Long accountId) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));
        return mapToDTO(account);
    }

    public List<BankAccountDTO> getBankAccountsByOrganization(Long organizationId) {
        return bankAccountRepository.findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public BankAccountDTO updateBankAccount(Long accountId, BankAccountDTO dto) {
        BankAccount account = bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));

        if (dto.getBankName() != null) account.setBankName(dto.getBankName());
        if (dto.getAccountHolder() != null) account.setAccountHolder(dto.getAccountHolder());
        if (dto.getBalance() != null) account.setBalance(dto.getBalance());
        if (dto.getActive() != null) account.setActive(dto.getActive());

        return mapToDTO(bankAccountRepository.save(account));
    }

    // Controller-friendly aliases
    public BankAccountDTO getBankAccountById(Long accountId) {
        return getBankAccount(accountId);
    }

    public BankAccountDTO reconcileAccount(Long accountId) {
        return getBankAccount(accountId);
    }

    private BankAccountDTO mapToDTO(BankAccount account) {
        return BankAccountDTO.builder()
                .id(account.getId())
                .organizationId(account.getOrganization().getId())
                .chartAccountId(account.getChartAccount().getId())
                .bankName(account.getBankName())
                .accountNumber(account.getAccountNumber())
                .accountHolder(account.getAccountHolder())
                .branchCode(account.getBranchCode())
                .routingNumber(account.getRoutingNumber())
                .swiftCode(account.getSwiftCode())
                .ibanCode(account.getIbanCode())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .active(account.getActive())
                .notes(account.getNotes())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}

