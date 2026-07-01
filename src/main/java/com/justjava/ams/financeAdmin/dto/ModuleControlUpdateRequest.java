package com.justjava.ams.financeAdmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleControlUpdateRequest {

    private Boolean enabled;
    private Boolean allowUserConfiguration;
    private Boolean requiresApproval;
    private Boolean enableAuditTrail;
    private Boolean enableNotifications;
    private Integer maxTransactionAmountLimit;
    private String configurationJson;
    private String notes;
}
