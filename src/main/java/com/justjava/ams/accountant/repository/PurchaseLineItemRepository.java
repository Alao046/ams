package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.PurchaseLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseLineItemRepository extends JpaRepository<PurchaseLineItem, Long> {
    List<PurchaseLineItem> findByPurchaseInvoiceId(Long purchaseInvoiceId);
}

