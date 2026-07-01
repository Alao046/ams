package com.justjava.ams.auditor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEventAcknowledgeRequest {

    private String acknowledgedBy;
    private String note;
}
