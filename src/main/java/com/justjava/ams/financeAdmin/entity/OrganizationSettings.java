package com.justjava.ams.financeAdmin.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.justjava.ams.common.entity.Organization;

import java.time.LocalDateTime;

@Entity
@Table(name = "organization_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "organization_id", nullable = false, unique = true)
    private Organization organization;

    @Column(nullable = false)
    private String companyLogo;

    @Column(nullable = false)
    private String companyWebsite;

    @Column(nullable = false)
    private Boolean enableTwoFactorAuth = true;

    @Column(nullable = false)
    private Boolean enablePasswordExpiry = true;

    @Column(nullable = false)
    private Integer passwordExpiryDays = 90;

    @Column(nullable = false)
    private Boolean enableIPWhitelist = false;

    @Column
    private String ipWhitelist;

    @Column(nullable = false)
    private Boolean enableSessionTimeout = true;

    @Column(nullable = false)
    private Integer sessionTimeoutMinutes = 30;

    @Column(nullable = false)
    private String dateFormat = "MM/dd/yyyy";

    @Column(nullable = false)
    private String timeFormat = "HH:mm:ss";

    @Column(nullable = false)
    private String timezone = "UTC";

    @Column(nullable = false)
    private String locale = "en_US";

    @Column
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

