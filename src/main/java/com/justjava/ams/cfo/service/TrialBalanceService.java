package com.justjava.ams.cfo.service;

import com.justjava.ams.cfo.dto.TrialBalanceDTO;
import com.justjava.ams.cfo.entity.TrialBalance;
import com.justjava.ams.cfo.repository.TrialBalanceRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TrialBalanceService {

    private final TrialBalanceRepository trialBalanceRepository;
    private final OrganizationRepository organizationRepository;

    public TrialBalanceDTO createTrialBalance(Long organizationId, TrialBalanceDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        TrialBalance trialBalance = TrialBalance.builder()
                .organization(organization)
                .reportDate(dto.getReportDate())
                .accountCode(dto.getAccountCode())
                .accountName(dto.getAccountName())
                .debitBalance(dto.getDebitBalance())
                .creditBalance(dto.getCreditBalance())
                .notes(dto.getNotes())
                .build();

        return mapToDTO(trialBalanceRepository.save(trialBalance));
    }

    public TrialBalanceDTO getTrialBalance(Long trialBalanceId) {
        TrialBalance trialBalance = trialBalanceRepository.findById(trialBalanceId)
                .orElseThrow(() -> new RuntimeException("Trial balance not found"));
        return mapToDTO(trialBalance);
    }

    public List<TrialBalanceDTO> getByReportDate(Long organizationId, LocalDate reportDate) {
        return trialBalanceRepository.findByOrganizationIdAndReportDate(organizationId, reportDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TrialBalanceDTO> getByStatus(Long organizationId, String status) {
        return trialBalanceRepository.findByOrganizationIdAndStatus(organizationId, TrialBalance.ReportStatus.valueOf(status))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
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
                .status(trialBalance.getStatus().toString())
                .notes(trialBalance.getNotes())
                .createdAt(trialBalance.getCreatedAt())
                .updatedAt(trialBalance.getUpdatedAt())
                .build();
    }
}

