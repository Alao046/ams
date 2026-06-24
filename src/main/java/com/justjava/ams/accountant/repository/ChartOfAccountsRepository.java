package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.ChartOfAccounts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChartOfAccountsRepository extends JpaRepository<ChartOfAccounts, Long> {
    Optional<ChartOfAccounts> findByOrganizationIdAndAccountCode(Long organizationId, String accountCode);
    Optional<ChartOfAccounts> findByOrganizationIdAndAccountCodeIgnoreCase(Long organizationId, String accountCode);
    boolean existsByOrganizationIdAndAccountCodeIgnoreCase(Long organizationId, String accountCode);
    List<ChartOfAccounts> findByOrganizationIdAndAccountType(Long organizationId, ChartOfAccounts.AccountType accountType);
    List<ChartOfAccounts> findByOrganizationIdAndActiveTrue(Long organizationId);
}

