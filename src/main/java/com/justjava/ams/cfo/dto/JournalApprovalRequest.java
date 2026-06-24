package com.justjava.ams.cfo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JournalApprovalRequest {
    private String approvedBy;
    private String approvalNote;
}
