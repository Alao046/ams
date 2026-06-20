package com.justjava.ams.cfo.repository;

import com.justjava.ams.cfo.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByOrganizationIdAndBudgetCode(Long organizationId, String budgetCode);
    List<Budget> findByOrganizationIdAndBudgetYear(Long organizationId, Integer year);
    List<Budget> findByOrganizationIdAndStatus(Long organizationId, Budget.BudgetStatus status);

    // Budget variance analysis queries
    List<Budget> findByOrganizationId(Long organizationId);
}
