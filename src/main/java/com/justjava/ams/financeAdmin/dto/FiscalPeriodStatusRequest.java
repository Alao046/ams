package com.justjava.ams.financeAdmin.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FiscalPeriodStatusRequest {
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;
}
