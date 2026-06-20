package com.justjava.ams.auditor.repository;

import com.justjava.ams.auditor.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByOrganizationIdAndEntityTypeAndEntityId(Long organizationId, String entityType, Long entityId);
    List<AuditLog> findByOrganizationIdAndCreatedAtBetween(Long organizationId, LocalDateTime startDate, LocalDateTime endDate);
    List<AuditLog> findByUserId(Long userId);
    List<AuditLog> findByOrganizationId(Long organizationId);
}

