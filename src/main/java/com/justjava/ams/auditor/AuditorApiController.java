package com.justjava.ams.auditor;

import com.justjava.ams.auditor.dto.AuditLogDTO;
import com.justjava.ams.auditor.dto.AuditLogFilterRequest;
import com.justjava.ams.auditor.dto.AuditLogResponse;
import com.justjava.ams.auditor.dto.SecurityEventAcknowledgeRequest;
import com.justjava.ams.auditor.dto.SecurityEventDTO;
import com.justjava.ams.auditor.dto.SecurityEventFilterRequest;
import com.justjava.ams.auditor.dto.SecurityEventResponse;
import com.justjava.ams.auditor.service.AuditLogService;
import com.justjava.ams.auditor.service.SecurityEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auditor")
@RequiredArgsConstructor
public class AuditorApiController {
    private final AuditLogService auditLogService;
    private final SecurityEventService securityEventService;

    @GetMapping("/audit-logs/org/{organizationId}")
    public List<AuditLogResponse> getAuditLogs(
            @PathVariable Long organizationId,
            @ModelAttribute AuditLogFilterRequest filter) {
        return auditLogService.getLogs(organizationId, filter)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/security-events/org/{organizationId}")
    public List<SecurityEventResponse> getSecurityEvents(
            @PathVariable Long organizationId,
            @ModelAttribute SecurityEventFilterRequest filter) {
        List<SecurityEventDTO> events = securityEventService.getEvents(organizationId, filter);
        return events.stream().map(this::toSecurityEventResponse).collect(Collectors.toList());
    }

    @GetMapping("/security-events/org/{organizationId}/unacknowledged")
    public List<SecurityEventResponse> getUnacknowledgedSecurityEvents(@PathVariable Long organizationId) {
        List<SecurityEventDTO> events = securityEventService.getUnacknowledgedEvents(organizationId);
        return events.stream().map(this::toSecurityEventResponse).collect(Collectors.toList());
    }

    @GetMapping("/security-events/org/{organizationId}/date-range")
    public List<SecurityEventResponse> getSecurityEventsByDateRange(
            @PathVariable Long organizationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SecurityEventDTO> events = securityEventService.getEventsByDateRange(organizationId, startDate, endDate);
        return events.stream().map(this::toSecurityEventResponse).collect(Collectors.toList());
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

    private SecurityEventResponse toSecurityEventResponse(SecurityEventDTO dto) {
        return SecurityEventResponse.builder()
                .id(dto.getId())
                .organizationId(dto.getOrganizationId())
                .organizationName(null)
                .userId(dto.getUserId())
                .eventType(dto.getEventType())
                .severity(dto.getSeverity())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .ipAddress(dto.getIpAddress())
                .userAgent(dto.getUserAgent())
                .acknowledged(dto.getAcknowledged())
                .acknowledgedAt(dto.getAcknowledgedAt())
                .acknowledgedBy(dto.getAcknowledgedBy())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    @PatchMapping("/security-events/{eventId}/acknowledge")
    public SecurityEventResponse acknowledgeSecurityEvent(
            @PathVariable Long eventId,
            @RequestBody(required = false) SecurityEventAcknowledgeRequest request,
            Principal principal) {
        String acknowledgedBy = principal != null ? principal.getName() : null;
        if ((acknowledgedBy == null || acknowledgedBy.trim().isEmpty())
                && request != null && request.getAcknowledgedBy() != null && !request.getAcknowledgedBy().trim().isEmpty()) {
            acknowledgedBy = request.getAcknowledgedBy();
        }
        if (acknowledgedBy == null || acknowledgedBy.trim().isEmpty()) {
            acknowledgedBy = "system";
        }

        SecurityEventDTO acknowledged = securityEventService.acknowledgeEvent(eventId, acknowledgedBy);
        return toSecurityEventResponse(acknowledged);
    }
}
