package com.justjava.ams.auditor.service;

import com.justjava.ams.auditor.dto.SecurityEventDTO;
import com.justjava.ams.auditor.entity.SecurityEvent;
import com.justjava.ams.auditor.repository.SecurityEventRepository;
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
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;
    private final OrganizationRepository organizationRepository;

    public SecurityEventDTO createSecurityEvent(Long organizationId, SecurityEventDTO dto) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        SecurityEvent event = SecurityEvent.builder()
                .organization(organization)
                .eventType(SecurityEvent.EventType.valueOf(dto.getEventType()))
                .severity(SecurityEvent.SeverityLevel.valueOf(dto.getSeverity()))
                .title(dto.getTitle())
                .description(dto.getDescription())
                .ipAddress(dto.getIpAddress())
                .userAgent(dto.getUserAgent())
                .build();

        return mapToDTO(securityEventRepository.save(event));
    }

    public SecurityEventDTO getSecurityEvent(Long eventId) {
        SecurityEvent event = securityEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Security event not found"));
        return mapToDTO(event);
    }

    public List<SecurityEventDTO> getEventsBySeverity(Long organizationId, String severity) {
        return securityEventRepository.findByOrganizationIdAndSeverity(organizationId, SecurityEvent.SeverityLevel.valueOf(severity))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<SecurityEventDTO> getEventsByType(Long organizationId, String eventType) {
        return securityEventRepository.findByOrganizationIdAndEventType(organizationId, SecurityEvent.EventType.valueOf(eventType))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<SecurityEventDTO> getUnacknowledgedEvents(Long organizationId) {
        return securityEventRepository.findByOrganizationIdAndAcknowledgedFalse(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<SecurityEventDTO> getEventsByDateRange(Long organizationId, LocalDateTime startDate, LocalDateTime endDate) {
        return securityEventRepository.findByOrganizationIdAndCreatedAtBetween(organizationId, startDate, endDate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public SecurityEventDTO acknowledgeEvent(Long eventId, String acknowledgedBy) {
        SecurityEvent event = securityEventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Security event not found"));
        event.setAcknowledged(true);
        event.setAcknowledgedAt(LocalDateTime.now());
        event.setAcknowledgedBy(acknowledgedBy);
        return mapToDTO(securityEventRepository.save(event));
    }

    public List<SecurityEventDTO> getAllEvents(Long organizationId) {
        return securityEventRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private SecurityEventDTO mapToDTO(SecurityEvent event) {
        return SecurityEventDTO.builder()
                .id(event.getId())
                .organizationId(event.getOrganization().getId())
                .userId(event.getUser() != null ? event.getUser().getId() : null)
                .eventType(event.getEventType().toString())
                .severity(event.getSeverity().toString())
                .title(event.getTitle())
                .description(event.getDescription())
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .acknowledged(event.getAcknowledged())
                .acknowledgedAt(event.getAcknowledgedAt())
                .acknowledgedBy(event.getAcknowledgedBy())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}

