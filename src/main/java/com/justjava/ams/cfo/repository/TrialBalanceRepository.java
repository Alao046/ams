package com.justjava.ams.cfo.repository;

import com.justjava.ams.cfo.entity.TrialBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrialBalanceRepository extends JpaRepository<TrialBalance, Long> {
    List<TrialBalance> findByOrganizationIdAndReportDate(Long organizationId, LocalDate reportDate);
    List<TrialBalance> findByOrganizationIdAndStatus(Long organizationId, TrialBalance.ReportStatus status);
}

