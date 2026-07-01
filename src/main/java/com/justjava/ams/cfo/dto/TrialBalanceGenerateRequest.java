package com.justjava.ams.cfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrialBalanceGenerateRequest {

    private LocalDate asOfDate;
    private Boolean persistSnapshot;
    private String generatedBy;
}
