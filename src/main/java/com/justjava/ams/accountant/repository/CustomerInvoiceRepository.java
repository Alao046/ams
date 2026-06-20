package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.CustomerInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerInvoiceRepository extends JpaRepository<CustomerInvoice, Long> {
    Optional<CustomerInvoice> findByOrganizationIdAndInvoiceNumber(Long organizationId, String invoiceNumber);
    List<CustomerInvoice> findByOrganizationIdAndStatus(Long organizationId, CustomerInvoice.InvoiceStatus status);
    List<CustomerInvoice> findByOrganizationId(Long organizationId);
    List<CustomerInvoice> findByOrganizationIdAndInvoiceDateBetween(Long organizationId, LocalDate startDate, LocalDate endDate);
}

