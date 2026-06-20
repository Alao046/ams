package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, Long> {
    List<FiscalPeriod> findByOrganizationIdAndYearOrderByQuarterAsc(Long organizationId, Integer year);
    Optional<FiscalPeriod> findByOrganizationIdAndYearAndQuarter(Long organizationId, Integer year, Integer quarter);
    List<FiscalPeriod> findByOrganizationId(Long organizationId);
}

