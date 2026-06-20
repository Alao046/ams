package com.justjava.ams.financeAdmin.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalConfigurationDTO {

    private Long id;
    private Long organizationId;
    private Integer fiscalYearStartMonth;
    private Integer fiscalYearEndMonth;
    private Integer numberOfQuarters;
    private String accountingMethod;
    private Boolean multiCurrencyEnabled;
    private String baseCurrency;
    private Boolean allowNegativeInventory;
    private Boolean requireApprovalForTransactions;
    private Integer approvalHierarchyLevels;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

