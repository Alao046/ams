package com.justjava.ams.accountant.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FixedAssetDTO {

    private Long id;
    private Long organizationId;
    private Long chartAccountId;
    private String assetCode;
    private String assetName;
    private String description;
    private String category;
    private BigDecimal originalCost;
    private LocalDate acquisitionDate;
    private LocalDate disposalDate;
    private BigDecimal depreciation;
    private BigDecimal depreciationRate;
    private String depreciationMethod;
    private Boolean active;
    private String location;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

