package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {
    Optional<PurchaseInvoice> findByOrganizationIdAndPurchaseOrderNumber(Long organizationId, String purchaseOrderNumber);
    List<PurchaseInvoice> findByOrganizationId(Long organizationId);
    List<PurchaseInvoice> findByOrganizationIdAndStatus(Long organizationId, PurchaseInvoice.PurchaseStatus status);
    List<PurchaseInvoice> findByOrganizationIdAndPurchaseDateBetween(Long organizationId, LocalDate startDate, LocalDate endDate);
}

