package com.justjava.ams.accountant.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseInvoiceDTO {

    private Long id;
    private Long organizationId;
    private String purchaseOrderNumber;
    private String vendorName;
    private String vendorEmail;
    private String vendorPhone;
    private String vendorAddress;
    private LocalDate purchaseDate;
    private LocalDate dueDate;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private String status;
    private String notes;
    private Long createdByUserId;
    private Set<PurchaseLineItemDTO> lineItems;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

