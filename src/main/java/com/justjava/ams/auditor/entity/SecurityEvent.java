package com.justjava.ams.auditor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SeverityLevel severity;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String ipAddress;

    @Column
    private String userAgent;

    @Column
    private Boolean acknowledged = false;

    @Column
    private LocalDateTime acknowledgedAt;

    @Column
    private String acknowledgedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum EventType {
        UNAUTHORIZED_ACCESS,
        MULTIPLE_LOGIN_ATTEMPTS,
        PERMISSION_CHANGE,
        DATA_ACCESS,
        DATA_MODIFICATION,
        DATA_DELETION,
        CONFIGURATION_CHANGE,
        PAYMENT_PROCESSING,
        REPORT_GENERATION,
        EXPORT_DATA,
        SUSPICIOUS_ACTIVITY,
        OTHER
    }

    public enum SeverityLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}

