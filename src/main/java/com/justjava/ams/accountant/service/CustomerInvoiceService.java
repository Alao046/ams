package com.justjava.ams.accountant.service;

import com.justjava.ams.accountant.dto.CustomerInvoiceDTO;
import com.justjava.ams.accountant.entity.CustomerInvoice;
import com.justjava.ams.accountant.repository.CustomerInvoiceRepository;
import com.justjava.ams.common.entity.Organization;
import com.justjava.ams.common.entity.User;
import com.justjava.ams.common.repository.OrganizationRepository;
import com.justjava.ams.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerInvoiceService {

    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public CustomerInvoiceDTO createInvoice(Long organizationId, CustomerInvoiceDTO dto, Long userId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new RuntimeException("Organization not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomerInvoice invoice = CustomerInvoice.builder()
                .organization(organization)
                .invoiceNumber(dto.getInvoiceNumber())
                .customerName(dto.getCustomerName())
                .customerEmail(dto.getCustomerEmail())
                .customerPhone(dto.getCustomerPhone())
                .customerAddress(dto.getCustomerAddress())
                .invoiceDate(dto.getInvoiceDate())
                .dueDate(dto.getDueDate())
                .subtotal(dto.getSubtotal())
                .taxAmount(dto.getTaxAmount())
                .totalAmount(dto.getTotalAmount())
                .status(CustomerInvoice.InvoiceStatus.DRAFT)
                .notes(dto.getNotes())
                .createdByUser(user)
                .build();

        return mapToDTO(customerInvoiceRepository.save(invoice));
    }

    public CustomerInvoiceDTO getInvoice(Long invoiceId) {
        CustomerInvoice invoice = customerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return mapToDTO(invoice);
    }

    public List<CustomerInvoiceDTO> getInvoicesByStatus(Long organizationId, String status) {
        return customerInvoiceRepository.findByOrganizationIdAndStatus(
                organizationId,
                CustomerInvoice.InvoiceStatus.valueOf(status))
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public CustomerInvoiceDTO updateInvoiceStatus(Long invoiceId, String status) {
        CustomerInvoice invoice = customerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setStatus(CustomerInvoice.InvoiceStatus.valueOf(status));
        return mapToDTO(customerInvoiceRepository.save(invoice));
    }

    public void deleteInvoice(Long invoiceId) {
        CustomerInvoice invoice = customerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setStatus(CustomerInvoice.InvoiceStatus.CANCELLED);
        customerInvoiceRepository.save(invoice);
    }

    private CustomerInvoiceDTO mapToDTO(CustomerInvoice invoice) {
        return CustomerInvoiceDTO.builder()
                .id(invoice.getId())
                .organizationId(invoice.getOrganization().getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerName(invoice.getCustomerName())
                .customerEmail(invoice.getCustomerEmail())
                .customerPhone(invoice.getCustomerPhone())
                .customerAddress(invoice.getCustomerAddress())
                .invoiceDate(invoice.getInvoiceDate())
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

    // Controller-friendly aliases and helpers
    public CustomerInvoiceDTO createCustomerInvoice(Long organizationId, CustomerInvoiceDTO dto) {
        Long userId = resolveCurrentUserId();
        return createInvoice(organizationId, dto, userId);
    }

    public CustomerInvoiceDTO getCustomerInvoiceById(Long invoiceId) {
        return getInvoice(invoiceId);
    }

    public List<CustomerInvoiceDTO> getCustomerInvoicesByOrganization(Long organizationId) {
        return customerInvoiceRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<CustomerInvoiceDTO> getInvoicesByOrganization(Long organizationId) {
        return getCustomerInvoicesByOrganization(organizationId);
    }

    public List<CustomerInvoiceDTO> getCustomerInvoicesByOrganizationAndStatus(Long organizationId, String status) {
        return getInvoicesByStatus(organizationId, status);
    }

    public List<CustomerInvoiceDTO> getInvoicesByOrganizationAndStatus(Long organizationId, String status) {
        return getCustomerInvoicesByOrganizationAndStatus(organizationId, status);
    }

    public CustomerInvoiceDTO updateCustomerInvoice(Long invoiceId, CustomerInvoiceDTO dto) {
        CustomerInvoice invoice = customerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (dto.getInvoiceNumber() != null) invoice.setInvoiceNumber(dto.getInvoiceNumber());
        if (dto.getCustomerName() != null) invoice.setCustomerName(dto.getCustomerName());
        if (dto.getCustomerEmail() != null) invoice.setCustomerEmail(dto.getCustomerEmail());
        if (dto.getCustomerPhone() != null) invoice.setCustomerPhone(dto.getCustomerPhone());
        if (dto.getCustomerAddress() != null) invoice.setCustomerAddress(dto.getCustomerAddress());
        if (dto.getInvoiceDate() != null) invoice.setInvoiceDate(dto.getInvoiceDate());
        if (dto.getDueDate() != null) invoice.setDueDate(dto.getDueDate());
        if (dto.getSubtotal() != null) invoice.setSubtotal(dto.getSubtotal());
        if (dto.getTaxAmount() != null) invoice.setTaxAmount(dto.getTaxAmount());
        if (dto.getTotalAmount() != null) invoice.setTotalAmount(dto.getTotalAmount());
        if (dto.getNotes() != null) invoice.setNotes(dto.getNotes());

        return mapToDTO(customerInvoiceRepository.save(invoice));
    }

    public CustomerInvoiceDTO updateInvoice(Long invoiceId, CustomerInvoiceDTO dto) {
        return updateCustomerInvoice(invoiceId, dto);
    }

    public CustomerInvoiceDTO generateInvoice(Long invoiceId) {
        CustomerInvoice invoice = customerInvoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        invoice.setStatus(CustomerInvoice.InvoiceStatus.SENT);
        return mapToDTO(customerInvoiceRepository.save(invoice));
    }

    public CustomerInvoiceDTO postInvoice(Long invoiceId) {
        return generateInvoice(invoiceId);
    }

    private Long resolveCurrentUserId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null) {
                return userRepository.findByUsername(auth.getName())
                        .map(u -> u.getId())
                        .orElse(null);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
