package com.justjava.ams.accountant.repository;

import com.justjava.ams.accountant.entity.JournalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalLineRepository extends JpaRepository<JournalLine, Long> {
	List<JournalLine> findByManualJournalId(Long manualJournalId);
}


