package com.justjava.ams.auditor.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDTO {

    private Long id;
    private Long organizationId;
    private Long userId;
    private String entityType;
    private Long entityId;
    private String action;
    private String oldValue;
    private String newValue;
    private String ipAddress;
    private String userAgent;
    private String description;
    private LocalDateTime createdAt;
}

