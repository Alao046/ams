package com.justjava.ams.cfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialReportApprovalRequest {

    private String approvedBy;
    private String approvalNote;
}
