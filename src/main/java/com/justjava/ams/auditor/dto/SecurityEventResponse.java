package com.justjava.ams.auditor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEventResponse {

    private Long id;
    private Long organizationId;
    private String organizationName;
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
