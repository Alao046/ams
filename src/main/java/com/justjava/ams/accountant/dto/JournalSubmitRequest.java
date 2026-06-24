package com.justjava.ams.accountant.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JournalSubmitRequest {
    @Size(max = 255, message = "Submitted by must not exceed 255 characters")
    private String submittedBy;
}
