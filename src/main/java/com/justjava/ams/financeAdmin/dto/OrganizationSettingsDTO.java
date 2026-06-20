package com.justjava.ams.financeAdmin.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationSettingsDTO {

    private Long id;
    private Long organizationId;
    private String companyLogo;
    private String companyWebsite;
    private Boolean enableTwoFactorAuth;
    private Boolean enablePasswordExpiry;
    private Integer passwordExpiryDays;
    private Boolean enableIPWhitelist;
    private String ipWhitelist;
    private Boolean enableSessionTimeout;
    private Integer sessionTimeoutMinutes;
    private String dateFormat;
    private String timeFormat;
    private String timezone;
    private String locale;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

