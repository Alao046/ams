package com.justjava.ams.financeAdmin.repository;

import com.justjava.ams.financeAdmin.entity.FiscalConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FiscalConfigurationRepository extends JpaRepository<FiscalConfiguration, Long> {
    Optional<FiscalConfiguration> findByOrganizationId(Long organizationId);
}
