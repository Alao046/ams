package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByOrganizationIdAndAccountNumber(Long organizationId, String accountNumber);
    List<BankAccount> findByOrganizationIdAndActiveTrue(Long organizationId);
}

