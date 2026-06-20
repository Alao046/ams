package com.justjava.ams.cfo.service;

import com.justjava.ams.cfo.dto.BudgetDTO;
import com.justjava.ams.cfo.entity.Budget;
import com.justjava.ams.cfo.repository.BudgetRepository;
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
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final OrganizationRepository organizationRepository;

    public BudgetDTO createBudget(Long organizationId, BudgetDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        Budget budget = Budget.builder()
                .organization(organization)
                .budgetCode(dto.getBudgetCode())
                .budgetName(dto.getBudgetName())
                .budgetYear(dto.getBudgetYear())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalBudget(dto.getTotalBudget())
                .departmentName(dto.getDepartmentName())
                .notes(dto.getNotes())
                .build();

        return mapToDTO(budgetRepository.save(budget));
    }

    public BudgetDTO getBudget(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        return mapToDTO(budget);
    }

    public List<BudgetDTO> getBudgetsByYear(Long organizationId, Integer year) {
        return budgetRepository.findByOrganizationIdAndBudgetYear(organizationId, year)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<BudgetDTO> getBudgetsByStatus(Long organizationId, String status) {
        return budgetRepository.findByOrganizationIdAndStatus(organizationId, Budget.BudgetStatus.valueOf(status))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public BudgetDTO updateBudgetStatus(Long budgetId, String status) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budget.setStatus(Budget.BudgetStatus.valueOf(status));
        return mapToDTO(budgetRepository.save(budget));
    }

    public BudgetDTO approveBudget(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budget.setApproved(true);
        budget.setStatus(Budget.BudgetStatus.APPROVED);
        return mapToDTO(budgetRepository.save(budget));
    }

    private BudgetDTO mapToDTO(Budget budget) {
        return BudgetDTO.builder()
                .id(budget.getId())
                .organizationId(budget.getOrganization().getId())
                .budgetCode(budget.getBudgetCode())
                .budgetName(budget.getBudgetName())
                .budgetYear(budget.getBudgetYear())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .totalBudget(budget.getTotalBudget())
                .allocatedAmount(budget.getAllocatedAmount())
                .spentAmount(budget.getSpentAmount())
                .status(budget.getStatus().toString())
                .approved(budget.getApproved())
                .departmentName(budget.getDepartmentName())
                .notes(budget.getNotes())
                .createdAt(budget.getCreatedAt())
                .updatedAt(budget.getUpdatedAt())
                .build();
    }
}

