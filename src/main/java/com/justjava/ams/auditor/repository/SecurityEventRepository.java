package com.justjava.ams.auditor.repository;

import com.justjava.ams.auditor.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
    List<SecurityEvent> findByOrganizationIdAndEventType(Long organizationId, SecurityEvent.EventType eventType);
    List<SecurityEvent> findByOrganizationIdAndSeverity(Long organizationId, SecurityEvent.SeverityLevel severity);
    List<SecurityEvent> findByOrganizationIdAndCreatedAtBetween(Long organizationId, LocalDateTime startDate, LocalDateTime endDate);
    List<SecurityEvent> findByOrganizationIdAndAcknowledgedFalse(Long organizationId);
    List<SecurityEvent> findByOrganizationId(Long organizationId);

    // Ordered retrievals for auditor UI
    List<SecurityEvent> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<SecurityEvent> findByOrganizationIdAndAcknowledgedFalseOrderByCreatedAtDesc(Long organizationId);

    List<SecurityEvent> findByOrganizationIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long organizationId, LocalDateTime startDate, LocalDateTime endDate);
}

