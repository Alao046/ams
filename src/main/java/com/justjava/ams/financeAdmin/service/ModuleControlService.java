package com.justjava.ams.financeAdmin.service;

import com.justjava.ams.financeAdmin.dto.ModuleControlDTO;
import com.justjava.ams.financeAdmin.entity.ModuleControl;
import com.justjava.ams.financeAdmin.repository.ModuleControlRepository;
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
public class ModuleControlService {

    private final ModuleControlRepository moduleControlRepository;
    private final OrganizationRepository organizationRepository;

    public ModuleControlDTO createModuleControl(Long organizationId, ModuleControlDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        ModuleControl module = ModuleControl.builder()
                .organization(organization)
                .moduleType(ModuleControl.ModuleType.valueOf(dto.getModuleType()))
                .enabled(dto.getEnabled() != null ? dto.getEnabled() : true)
                .allowUserConfiguration(dto.getAllowUserConfiguration() != null ? dto.getAllowUserConfiguration() : false)
                .requiresApproval(dto.getRequiresApproval() != null ? dto.getRequiresApproval() : false)
                .enableAuditTrail(dto.getEnableAuditTrail() != null ? dto.getEnableAuditTrail() : true)
                .enableNotifications(dto.getEnableNotifications() != null ? dto.getEnableNotifications() : true)
                .maxTransactionAmountLimit(dto.getMaxTransactionAmountLimit())
                .configurationJson(dto.getConfigurationJson())
                .notes(dto.getNotes())
                .build();

        return mapToDTO(moduleControlRepository.save(module));
    }

    public ModuleControlDTO getModuleControl(Long moduleId) {
        ModuleControl module = moduleControlRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module control not found"));
        return mapToDTO(module);
    }

    public ModuleControlDTO getByOrganizationAndType(Long organizationId, String moduleType) {
        ModuleControl module = moduleControlRepository.findByOrganizationIdAndModuleType(
                organizationId,
                ModuleControl.ModuleType.valueOf(moduleType))
                .orElseThrow(() -> new RuntimeException("Module control not found"));
        return mapToDTO(module);
    }

    public List<ModuleControlDTO> getModulesByOrganization(Long organizationId) {
        return moduleControlRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ModuleControlDTO> getEnabledModulesByOrganization(Long organizationId) {
        return moduleControlRepository.findByOrganizationIdAndEnabledTrue(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ModuleControlDTO updateModuleControl(Long moduleId, ModuleControlDTO dto) {
        ModuleControl module = moduleControlRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module control not found"));

        if (dto.getEnabled() != null) module.setEnabled(dto.getEnabled());
        if (dto.getAllowUserConfiguration() != null) module.setAllowUserConfiguration(dto.getAllowUserConfiguration());
        if (dto.getRequiresApproval() != null) module.setRequiresApproval(dto.getRequiresApproval());
        if (dto.getEnableAuditTrail() != null) module.setEnableAuditTrail(dto.getEnableAuditTrail());
        if (dto.getEnableNotifications() != null) module.setEnableNotifications(dto.getEnableNotifications());
        if (dto.getMaxTransactionAmountLimit() != null) module.setMaxTransactionAmountLimit(dto.getMaxTransactionAmountLimit());
        if (dto.getConfigurationJson() != null) module.setConfigurationJson(dto.getConfigurationJson());

        return mapToDTO(moduleControlRepository.save(module));
    }

    private ModuleControlDTO mapToDTO(ModuleControl module) {
        return ModuleControlDTO.builder()
                .id(module.getId())
                .organizationId(module.getOrganization().getId())
                .moduleType(module.getModuleType().toString())
                .enabled(module.getEnabled())
                .allowUserConfiguration(module.getAllowUserConfiguration())
                .requiresApproval(module.getRequiresApproval())
                .enableAuditTrail(module.getEnableAuditTrail())
                .enableNotifications(module.getEnableNotifications())
                .maxTransactionAmountLimit(module.getMaxTransactionAmountLimit())
                .configurationJson(module.getConfigurationJson())
                .notes(module.getNotes())
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }
}
