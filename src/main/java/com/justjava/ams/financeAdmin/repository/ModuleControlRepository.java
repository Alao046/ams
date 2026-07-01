package com.justjava.ams.financeAdmin.repository;

import com.justjava.ams.financeAdmin.entity.ModuleControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleControlRepository extends JpaRepository<ModuleControl, Long> {
    Optional<ModuleControl> findByOrganizationIdAndModuleType(Long organizationId, ModuleControl.ModuleType moduleType);
    List<ModuleControl> findByOrganizationIdAndEnabledTrue(Long organizationId);
    List<ModuleControl> findByOrganizationId(Long organizationId);

    boolean existsByOrganizationIdAndModuleType(Long organizationId, ModuleControl.ModuleType moduleType);
}
