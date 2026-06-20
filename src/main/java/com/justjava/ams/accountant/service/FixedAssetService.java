package com.justjava.ams.accountant.service;

import com.justjava.ams.accountant.dto.FixedAssetDTO;
import com.justjava.ams.accountant.entity.FixedAsset;
import com.justjava.ams.accountant.entity.ChartOfAccounts;
import com.justjava.ams.accountant.repository.FixedAssetRepository;
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
public class FixedAssetService {

    private final FixedAssetRepository fixedAssetRepository;
    private final ChartOfAccountsRepository chartOfAccountsRepository;
    private final OrganizationRepository organizationRepository;

    public FixedAssetDTO createFixedAsset(Long organizationId, FixedAssetDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        ChartOfAccounts account = chartOfAccountsRepository.findById(dto.getChartAccountId())
                .orElseThrow(() -> new RuntimeException("Chart account not found"));

        FixedAsset asset = FixedAsset.builder()
                .organization(organization)
                .chartAccount(account)
                .assetCode(dto.getAssetCode())
                .assetName(dto.getAssetName())
                .description(dto.getDescription())
                .category(FixedAsset.AssetCategory.valueOf(dto.getCategory()))
                .originalCost(dto.getOriginalCost())
                .acquisitionDate(dto.getAcquisitionDate())
                .depreciationRate(dto.getDepreciationRate())
                .depreciationMethod(FixedAsset.DepreciationMethod.valueOf(dto.getDepreciationMethod()))
                .active(true)
                .location(dto.getLocation())
                .notes(dto.getNotes())
                .build();

        return mapToDTO(fixedAssetRepository.save(asset));
    }

    public FixedAssetDTO getFixedAsset(Long assetId) {
        FixedAsset asset = fixedAssetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Fixed asset not found"));
        return mapToDTO(asset);
    }

    public List<FixedAssetDTO> getAssetsByOrganization(Long organizationId) {
        return fixedAssetRepository.findByOrganizationIdAndActiveTrue(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<FixedAssetDTO> getAssetsByCategory(Long organizationId, String category) {
        return fixedAssetRepository.findByOrganizationIdAndCategory(
                organizationId,
                FixedAsset.AssetCategory.valueOf(category))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public FixedAssetDTO updateFixedAsset(Long assetId, FixedAssetDTO dto) {
        FixedAsset asset = fixedAssetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Fixed asset not found"));

        if (dto.getAssetName() != null) asset.setAssetName(dto.getAssetName());
        if (dto.getDescription() != null) asset.setDescription(dto.getDescription());
        if (dto.getDepreciation() != null) asset.setDepreciation(dto.getDepreciation());
        if (dto.getActive() != null) asset.setActive(dto.getActive());

        return mapToDTO(fixedAssetRepository.save(asset));
    }

    public FixedAssetDTO calculateDepreciation(Long assetId) {
        FixedAsset asset = fixedAssetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Fixed asset not found"));

        if (asset.getDepreciationRate() == null || asset.getOriginalCost() == null) {
            throw new RuntimeException("Insufficient data to calculate depreciation");
        }

        // depreciationRate stored as percentage (e.g. 5 for 5%). Calculate depreciation amount.
        java.math.BigDecimal rate = asset.getDepreciationRate();
        java.math.BigDecimal depreciation = asset.getOriginalCost()
                .multiply(rate)
                .divide(java.math.BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);

        asset.setDepreciation(depreciation);
        FixedAsset saved = fixedAssetRepository.save(asset);
        return mapToDTO(saved);
    }

    private FixedAssetDTO mapToDTO(FixedAsset asset) {
        return FixedAssetDTO.builder()
                .id(asset.getId())
                .organizationId(asset.getOrganization() != null ? asset.getOrganization().getId() : null)
                .chartAccountId(asset.getChartAccount() != null ? asset.getChartAccount().getId() : null)
                .assetCode(asset.getAssetCode())
                .assetName(asset.getAssetName())
                .description(asset.getDescription())
                .category(asset.getCategory() != null ? asset.getCategory().toString() : null)
                .originalCost(asset.getOriginalCost())
                .acquisitionDate(asset.getAcquisitionDate())
                .disposalDate(asset.getDisposalDate())
                .depreciation(asset.getDepreciation())
                .depreciationRate(asset.getDepreciationRate())
                .depreciationMethod(asset.getDepreciationMethod() != null ? asset.getDepreciationMethod().toString() : null)
                .active(asset.getActive())
                .location(asset.getLocation())
                .notes(asset.getNotes())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}

