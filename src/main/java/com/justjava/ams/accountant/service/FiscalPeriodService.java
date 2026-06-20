package com.justjava.ams.accountant.service;

import com.justjava.ams.accountant.dto.FiscalPeriodDTO;
import com.justjava.ams.accountant.entity.FiscalPeriod;
import com.justjava.ams.accountant.repository.FiscalPeriodRepository;
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
public class FiscalPeriodService {

    private final FiscalPeriodRepository fiscalPeriodRepository;
    private final OrganizationRepository organizationRepository;

    public FiscalPeriodDTO createFiscalPeriod(Long organizationId, FiscalPeriodDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        FiscalPeriod period = FiscalPeriod.builder()
                .organization(organization)
                .year(dto.getYear())
                .quarter(dto.getQuarter())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(FiscalPeriod.PeriodStatus.OPEN)
                .closed(false)
                .build();

        return mapToDTO(fiscalPeriodRepository.save(period));
    }

    public FiscalPeriodDTO getFiscalPeriod(Long periodId) {
        FiscalPeriod period = fiscalPeriodRepository.findById(periodId)
                .orElseThrow(() -> new RuntimeException("Fiscal period not found"));
        return mapToDTO(period);
    }

    public List<FiscalPeriodDTO> getFiscalPeriodsByOrganization(Long organizationId) {
        return fiscalPeriodRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FiscalPeriodDTO getFiscalPeriodById(Long periodId) {
        return getFiscalPeriod(periodId);
    }

    public FiscalPeriodDTO getCurrentFiscalPeriod(Long organizationId) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        return fiscalPeriodRepository.findByOrganizationId(organizationId)
                .stream()
                .filter(p -> !p.getClosed() && (p.getStartDate().isBefore(now) || p.getStartDate().isEqual(now))
                        && (p.getEndDate().isAfter(now) || p.getEndDate().isEqual(now)))
                .findFirst()
                .map(this::mapToDTO)
                .orElse(null);
    }

    public FiscalPeriodDTO lockFiscalPeriod(Long periodId) {
        FiscalPeriod period = fiscalPeriodRepository.findById(periodId)
                .orElseThrow(() -> new RuntimeException("Fiscal period not found"));
        period.setStatus(FiscalPeriod.PeriodStatus.LOCKED);
        return mapToDTO(fiscalPeriodRepository.save(period));
    }

    public List<FiscalPeriodDTO> getPeriodsByYear(Long organizationId, Integer year) {
        return fiscalPeriodRepository.findByOrganizationIdAndYearOrderByQuarterAsc(organizationId, year)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FiscalPeriodDTO closeFiscalPeriod(Long periodId) {
        FiscalPeriod period = fiscalPeriodRepository.findById(periodId)
                .orElseThrow(() -> new RuntimeException("Fiscal period not found"));
        period.setStatus(FiscalPeriod.PeriodStatus.CLOSED);
        period.setClosed(true);
        period.setClosedDate(java.time.LocalDateTime.now());
        return mapToDTO(fiscalPeriodRepository.save(period));
    }

    private FiscalPeriodDTO mapToDTO(FiscalPeriod period) {
        return FiscalPeriodDTO.builder()
                .id(period.getId())
                .organizationId(period.getOrganization().getId())
                .year(period.getYear())
                .quarter(period.getQuarter())
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .status(period.getStatus().toString())
                .closed(period.getClosed())
                .closedDate(period.getClosedDate())
                .createdAt(period.getCreatedAt())
                .updatedAt(period.getUpdatedAt())
                .build();
    }
}

