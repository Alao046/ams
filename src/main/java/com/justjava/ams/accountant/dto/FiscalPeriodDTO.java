package com.justjava.ams.accountant.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiscalPeriodDTO {

    private Long id;
    private Long organizationId;
    private Integer year;
    private Integer quarter;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status;
    private Boolean closed;
    private LocalDateTime closedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

