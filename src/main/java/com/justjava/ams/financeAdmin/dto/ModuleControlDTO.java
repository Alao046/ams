package com.justjava.ams.financeAdmin.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleControlDTO {

    private Long id;
    private Long organizationId;
    private String moduleType;
    private Boolean enabled;
    private Boolean allowUserConfiguration;
    private Boolean requiresApproval;
    private Boolean enableAuditTrail;
    private Boolean enableNotifications;
    private Integer maxTransactionAmountLimit;
    private String configurationJson;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

