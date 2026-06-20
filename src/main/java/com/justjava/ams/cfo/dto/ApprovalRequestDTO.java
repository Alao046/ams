package com.justjava.ams.cfo.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequestDTO {

    private Long id;
    private Long organizationId;
    private String entityType;
    private Long entityId;
    private String status;
    private Long submittedByUserId;
    private LocalDate submittedDate;
    private Long assignedToUserId;
    private Long approvedByUserId;
    private LocalDate approvedDate;
    private String approvalNotes;
    private String rejectionReason;
    private Integer approvalLevel;
    private Integer requiredApprovals;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

