package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.FiscalPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FiscalPeriodRepository extends JpaRepository<FiscalPeriod, Long> {
    List<FiscalPeriod> findByOrganizationIdAndYearOrderByQuarterAsc(Long organizationId, Integer year);
    Optional<FiscalPeriod> findByOrganizationIdAndYearAndQuarter(Long organizationId, Integer year, Integer quarter);
    List<FiscalPeriod> findByOrganizationId(Long organizationId);

    @Query("""
            select fp from FiscalPeriod fp
            where fp.organization.id = :organizationId
              and fp.startDate <= :endDate
              and fp.endDate >= :startDate
            """)
    List<FiscalPeriod> findOverlappingPeriods(
            @Param("organizationId") Long organizationId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("""
            select fp from FiscalPeriod fp
            where fp.organization.id = :organizationId
              and fp.status = :status
              and fp.startDate <= :date
              and fp.endDate >= :date
            """)
    Optional<FiscalPeriod> findByOrganizationIdAndStatusContainingDate(
            @Param("organizationId") Long organizationId,
            @Param("status") FiscalPeriod.PeriodStatus status,
            @Param("date") LocalDateTime date);
}

