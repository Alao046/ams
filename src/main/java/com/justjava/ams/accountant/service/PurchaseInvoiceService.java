package com.justjava.ams.accountant.service;

import com.justjava.ams.accountant.dto.PurchaseInvoiceDTO;
import com.justjava.ams.accountant.entity.PurchaseInvoice;
import com.justjava.ams.accountant.repository.PurchaseInvoiceRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.entity.User;
import com.justjava.ams.common.repository.OrganizationRepository;
import com.justjava.ams.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseInvoiceService {

    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public PurchaseInvoiceDTO createPurchaseInvoice(Long organizationId, PurchaseInvoiceDTO dto, Long userId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PurchaseInvoice invoice = PurchaseInvoice.builder()
                .organization(organization)
                .purchaseOrderNumber(dto.getPurchaseOrderNumber())
                .vendorName(dto.getVendorName())
                .vendorEmail(dto.getVendorEmail())
                .vendorPhone(dto.getVendorPhone())
                .vendorAddress(dto.getVendorAddress())
                .purchaseDate(dto.getPurchaseDate())
                .dueDate(dto.getDueDate())
                .subtotal(dto.getSubtotal())
                .taxAmount(dto.getTaxAmount())
                .totalAmount(dto.getTotalAmount())
                .status(PurchaseInvoice.PurchaseStatus.PENDING)
                .notes(dto.getNotes())
                .createdByUser(user)
                .build();

        return mapToDTO(purchaseInvoiceRepository.save(invoice));
    }

    public PurchaseInvoiceDTO getPurchaseInvoice(Long invoiceId) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Purchase invoice not found"));
        return mapToDTO(invoice);
    }

    public List<PurchaseInvoiceDTO> getPurchaseInvoicesByStatus(Long organizationId, String status) {
        return purchaseInvoiceRepository.findByOrganizationIdAndStatus(
                organizationId,
                PurchaseInvoice.PurchaseStatus.valueOf(status))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public PurchaseInvoiceDTO updatePurchaseInvoiceStatus(Long invoiceId, String status) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Purchase invoice not found"));
        invoice.setStatus(PurchaseInvoice.PurchaseStatus.valueOf(status));
        return mapToDTO(purchaseInvoiceRepository.save(invoice));
    }

    // Controller-friendly aliases and helper methods
    public PurchaseInvoiceDTO getPurchaseInvoiceById(Long invoiceId) {
        return getPurchaseInvoice(invoiceId);
    }

    public List<PurchaseInvoiceDTO> getPurchaseInvoicesByOrganization(Long organizationId) {
        return purchaseInvoiceRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<PurchaseInvoiceDTO> getPurchaseInvoicesByOrganizationAndStatus(Long organizationId, String status) {
        return getPurchaseInvoicesByStatus(organizationId, status);
    }

    public PurchaseInvoiceDTO updatePurchaseInvoice(Long invoiceId, PurchaseInvoiceDTO dto) {
        PurchaseInvoice invoice = purchaseInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Purchase invoice not found"));

        if (dto.getPurchaseOrderNumber() != null) invoice.setPurchaseOrderNumber(dto.getPurchaseOrderNumber());
        if (dto.getVendorName() != null) invoice.setVendorName(dto.getVendorName());
        if (dto.getVendorEmail() != null) invoice.setVendorEmail(dto.getVendorEmail());
        if (dto.getVendorPhone() != null) invoice.setVendorPhone(dto.getVendorPhone());
        if (dto.getVendorAddress() != null) invoice.setVendorAddress(dto.getVendorAddress());
        if (dto.getPurchaseDate() != null) invoice.setPurchaseDate(dto.getPurchaseDate());
        if (dto.getDueDate() != null) invoice.setDueDate(dto.getDueDate());
        if (dto.getSubtotal() != null) invoice.setSubtotal(dto.getSubtotal());
        if (dto.getTaxAmount() != null) invoice.setTaxAmount(dto.getTaxAmount());
        if (dto.getTotalAmount() != null) invoice.setTotalAmount(dto.getTotalAmount());
        if (dto.getNotes() != null) invoice.setNotes(dto.getNotes());

        return mapToDTO(purchaseInvoiceRepository.save(invoice));
    }

    public PurchaseInvoiceDTO submitPurchaseInvoice(Long invoiceId) {
        return updatePurchaseInvoiceStatus(invoiceId, "SUBMITTED");
    }

    private PurchaseInvoiceDTO mapToDTO(PurchaseInvoice invoice) {
        return PurchaseInvoiceDTO.builder()
                .id(invoice.getId())
                .organizationId(invoice.getOrganization().getId())
                .purchaseOrderNumber(invoice.getPurchaseOrderNumber())
                .vendorName(invoice.getVendorName())
                .vendorEmail(invoice.getVendorEmail())
                .vendorPhone(invoice.getVendorPhone())
                .vendorAddress(invoice.getVendorAddress())
                .purchaseDate(invoice.getPurchaseDate())
                .dueDate(invoice.getDueDate())
                .subtotal(invoice.getSubtotal())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .amountPaid(invoice.getAmountPaid())
                .status(invoice.getStatus().toString())
                .notes(invoice.getNotes())
                .createdByUserId(invoice.getCreatedByUser() != null ? invoice.getCreatedByUser().getId() : null)
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}

