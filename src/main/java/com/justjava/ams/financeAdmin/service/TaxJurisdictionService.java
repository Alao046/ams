package com.justjava.ams.financeAdmin.service;

import com.justjava.ams.financeAdmin.dto.TaxJurisdictionDTO;
import com.justjava.ams.financeAdmin.entity.TaxJurisdiction;
import com.justjava.ams.financeAdmin.repository.TaxJurisdictionRepository;
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
public class TaxJurisdictionService {

    private final TaxJurisdictionRepository taxJurisdictionRepository;
    private final OrganizationRepository organizationRepository;

    public TaxJurisdictionDTO createTaxJurisdiction(Long organizationId, TaxJurisdictionDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        TaxJurisdiction jurisdiction = TaxJurisdiction.builder()
                .organization(organization)
                .jurisdictionName(dto.getJurisdictionName())
                .jurisdictionCode(dto.getJurisdictionCode())
                .country(dto.getCountry())
                .state(dto.getState())
                .municipality(dto.getMunicipality())
                .taxRate(dto.getTaxRate())
                .taxType(TaxJurisdiction.TaxType.valueOf(dto.getTaxType()))
                .calculationType(TaxJurisdiction.TaxCalculationType.valueOf(dto.getCalculationType()))
                .description(dto.getDescription())
                .active(true)
                .build();

        return mapToDTO(taxJurisdictionRepository.save(jurisdiction));
    }

    public TaxJurisdictionDTO getTaxJurisdiction(Long jurisdictionId) {
        TaxJurisdiction jurisdiction = taxJurisdictionRepository.findById(jurisdictionId)
                .orElseThrow(() -> new RuntimeException("Tax jurisdiction not found"));
        return mapToDTO(jurisdiction);
    }

    public List<TaxJurisdictionDTO> getJurisdictionsByOrganization(Long organizationId) {
        return taxJurisdictionRepository.findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<TaxJurisdictionDTO> getJurisdictionsByType(Long organizationId, String taxType) {
        return taxJurisdictionRepository.findByOrganizationIdAndTaxType(organizationId, TaxJurisdiction.TaxType.valueOf(taxType))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public TaxJurisdictionDTO updateTaxJurisdiction(Long jurisdictionId, TaxJurisdictionDTO dto) {
        TaxJurisdiction jurisdiction = taxJurisdictionRepository.findById(jurisdictionId)
                .orElseThrow(() -> new RuntimeException("Tax jurisdiction not found"));

        if (dto.getJurisdictionName() != null) jurisdiction.setJurisdictionName(dto.getJurisdictionName());
        if (dto.getTaxRate() != null) jurisdiction.setTaxRate(dto.getTaxRate());
        if (dto.getActive() != null) jurisdiction.setActive(dto.getActive());
        if (dto.getDescription() != null) jurisdiction.setDescription(dto.getDescription());

        return mapToDTO(taxJurisdictionRepository.save(jurisdiction));
    }

    public void deleteTaxJurisdiction(Long jurisdictionId) {
        TaxJurisdiction jurisdiction = taxJurisdictionRepository.findById(jurisdictionId)
                .orElseThrow(() -> new RuntimeException("Tax jurisdiction not found"));
        jurisdiction.setActive(false);
        taxJurisdictionRepository.save(jurisdiction);
    }

    private TaxJurisdictionDTO mapToDTO(TaxJurisdiction jurisdiction) {
        return TaxJurisdictionDTO.builder()
                .id(jurisdiction.getId())
                .organizationId(jurisdiction.getOrganization().getId())
                .jurisdictionName(jurisdiction.getJurisdictionName())
                .jurisdictionCode(jurisdiction.getJurisdictionCode())
                .country(jurisdiction.getCountry())
                .state(jurisdiction.getState())
                .municipality(jurisdiction.getMunicipality())
                .taxRate(jurisdiction.getTaxRate())
                .taxType(jurisdiction.getTaxType().toString())
                .calculationType(jurisdiction.getCalculationType().toString())
                .description(jurisdiction.getDescription())
                .active(jurisdiction.getActive())
                .createdAt(jurisdiction.getCreatedAt())
                .updatedAt(jurisdiction.getUpdatedAt())
                .build();
    }
}
