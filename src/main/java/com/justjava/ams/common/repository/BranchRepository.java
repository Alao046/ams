package com.justjava.ams.common.repository;

import com.justjava.ams.common.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    List<Branch> findByOrganizationId(Long organizationId);
    Optional<Branch> findByCode(String code);
    Optional<Branch> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
