package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.FixedAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FixedAssetRepository extends JpaRepository<FixedAsset, Long> {
    Optional<FixedAsset> findByOrganizationIdAndAssetCode(Long organizationId, String assetCode);
    List<FixedAsset> findByOrganizationIdAndActiveTrue(Long organizationId);
    List<FixedAsset> findByOrganizationIdAndCategory(Long organizationId, FixedAsset.AssetCategory category);
}

