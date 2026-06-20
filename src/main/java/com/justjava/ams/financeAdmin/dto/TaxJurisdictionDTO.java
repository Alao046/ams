package com.justjava.ams.financeAdmin.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaxJurisdictionDTO {

    private Long id;
    private Long organizationId;
    private String jurisdictionName;
    private String jurisdictionCode;
    private String country;
    private String state;
    private String municipality;
    private BigDecimal taxRate;
    private String taxType;
    private String calculationType;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

