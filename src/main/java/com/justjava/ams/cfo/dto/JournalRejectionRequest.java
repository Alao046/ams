package com.justjava.ams.cfo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JournalRejectionRequest {
    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;

    private String rejectedBy;
}
