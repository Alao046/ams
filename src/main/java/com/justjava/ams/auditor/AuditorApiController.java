package com.justjava.ams.auditor;

import com.justjava.ams.auditor.dto.AuditLogDTO;
import com.justjava.ams.auditor.dto.AuditLogFilterRequest;
import com.justjava.ams.auditor.dto.AuditLogResponse;
import com.justjava.ams.auditor.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auditor")
@RequiredArgsConstructor
public class AuditorApiController {
    private final AuditLogService auditLogService;

    @GetMapping("/audit-logs/org/{organizationId}")
    public List<AuditLogResponse> getAuditLogs(
            @PathVariable Long organizationId,
            @ModelAttribute AuditLogFilterRequest filter) {
        return auditLogService.getLogs(organizationId, filter)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/audit-logs/org/{organizationId}/entity")
    public List<AuditLogResponse> getAuditLogsByEntity(
            @PathVariable Long organizationId,
            @RequestParam String entityType,
            @RequestParam(required = false) Long entityId) {
        return auditLogService.getLogsByEntity(organizationId, entityType, entityId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/audit-logs/org/{organizationId}/date-range")
    public List<AuditLogResponse> getAuditLogsByDateRange(
            @PathVariable Long organizationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return auditLogService.getLogsByDateRange(organizationId, startDate, endDate)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AuditLogResponse toResponse(AuditLogDTO log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .organizationId(log.getOrganizationId())
                .userId(log.getUserId())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .action(log.getAction())
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
