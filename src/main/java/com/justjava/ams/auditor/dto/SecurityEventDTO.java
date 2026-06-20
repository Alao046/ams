package com.justjava.ams.auditor.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEventDTO {

    private Long id;
    private Long organizationId;
    private Long userId;
    private String eventType;
    private String severity;
    private String title;
    private String description;
    private String ipAddress;
    private String userAgent;
    private Boolean acknowledged;
    private LocalDateTime acknowledgedAt;
    private String acknowledgedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

