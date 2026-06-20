package com.justjava.ams.financeAdmin.repository;

import com.justjava.ams.financeAdmin.entity.TaxJurisdiction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaxJurisdictionRepository extends JpaRepository<TaxJurisdiction, Long> {
    Optional<TaxJurisdiction> findByOrganizationIdAndJurisdictionCode(Long organizationId, String jurisdictionCode);
    List<TaxJurisdiction> findByOrganizationIdAndActiveTrue(Long organizationId);
    List<TaxJurisdiction> findByOrganizationIdAndTaxType(Long organizationId, TaxJurisdiction.TaxType taxType);
}
