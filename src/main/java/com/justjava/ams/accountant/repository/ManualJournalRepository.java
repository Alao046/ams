package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.ManualJournal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ManualJournalRepository extends JpaRepository<ManualJournal, Long> {
	List<ManualJournal> findByOrganizationIdAndStatus(Long organizationId, ManualJournal.JournalStatus status);
	List<ManualJournal> findByOrganizationId(Long organizationId);
	List<ManualJournal> findByOrganizationIdAndJournalDateBetween(Long organizationId, LocalDate startDate, LocalDate endDate);
	List<ManualJournal> findByCreatedBy(String createdBy);
}


