package com.justjava.ams.cfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrialBalanceReportResponse {

    private Long organizationId;
    private LocalDate asOfDate;
    private List<TrialBalanceLineDTO> lines;
    private BigDecimal totalDebits;
    private BigDecimal totalCredits;
    private BigDecimal variance;
    private Boolean balanced;
    private String status;
    private LocalDateTime generatedAt;
}
