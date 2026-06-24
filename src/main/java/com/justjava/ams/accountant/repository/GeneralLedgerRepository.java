package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.GeneralLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface GeneralLedgerRepository extends JpaRepository<GeneralLedger, Long> {
    List<GeneralLedger> findByAccountIdOrderByTransactionDateDesc(Long accountId);
    List<GeneralLedger> findByJournalNumber(String journalNumber);
    List<GeneralLedger> findByJournalNumberAndStatus(String journalNumber, GeneralLedger.TransactionStatus status);
    List<GeneralLedger> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
    boolean existsByAccountIdAndStatus(Long accountId, GeneralLedger.TransactionStatus status);

    // Bank reconciliation queries
    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.transactionDate = :txDate AND gl.amount = :amount")
    List<GeneralLedger> findByTransactionDateAndAmount(@Param("txDate") LocalDate txDate, @Param("amount") BigDecimal amount);

    @Query("SELECT gl FROM GeneralLedger gl WHERE gl.account.id = :accountId AND gl.status = :status")
    List<GeneralLedger> findByAccountIdAndStatus(@Param("accountId") Long accountId, @Param("status") GeneralLedger.TransactionStatus status);
}
