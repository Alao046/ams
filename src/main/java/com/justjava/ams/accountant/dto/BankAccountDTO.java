package com.justjava.ams.accountant.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankAccountDTO {

    private Long id;
    private Long organizationId;
    private Long chartAccountId;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String branchCode;
    private String routingNumber;
    private String swiftCode;
    private String ibanCode;
    private String currency;
    private BigDecimal balance;
    private Boolean active;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

