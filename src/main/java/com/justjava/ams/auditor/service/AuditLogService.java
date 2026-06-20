package com.justjava.ams.auditor.service;

import com.justjava.ams.auditor.dto.AuditLogDTO;
import com.justjava.ams.auditor.entity.AuditLog;
import com.justjava.ams.auditor.repository.AuditLogRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final OrganizationRepository organizationRepository;

    public AuditLogDTO createAuditLog(Long organizationId, AuditLogDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        AuditLog log = AuditLog.builder()
                .organization(organization)
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .action(AuditLog.AuditAction.valueOf(dto.getAction()))
                .oldValue(dto.getOldValue())
                .newValue(dto.getNewValue())
                .ipAddress(dto.getIpAddress())
                .userAgent(dto.getUserAgent())
                .description(dto.getDescription())
                .build();

        return mapToDTO(auditLogRepository.save(log));
    }

    public AuditLogDTO getAuditLog(Long logId) {
        AuditLog log = auditLogRepository.findById(logId)
                .orElseThrow(() -> new RuntimeException("Audit log not found"));
        return mapToDTO(log);
    }

    public List<AuditLogDTO> getLogsByEntity(Long organizationId, String entityType, Long entityId) {
        return auditLogRepository.findByOrganizationIdAndEntityTypeAndEntityId(organizationId, entityType, entityId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AuditLogDTO> getLogsByDateRange(Long organizationId, LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByOrganizationIdAndCreatedAtBetween(organizationId, startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AuditLogDTO> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserId(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<AuditLogDTO> getAllLogs(Long organizationId) {
        return auditLogRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private AuditLogDTO mapToDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .organizationId(log.getOrganization().getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction().toString())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }
}

