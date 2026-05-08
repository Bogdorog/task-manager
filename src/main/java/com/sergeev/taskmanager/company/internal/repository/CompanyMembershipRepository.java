package com.sergeev.taskmanager.company.internal.repository;

import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {

    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

    Optional<CompanyMembership> findByUserIdAndCompanyId(Long userId, Long companyId);

    List<CompanyMembership> findAllByCompanyId(Long companyId);

    boolean existsByRoleId(Long roleId);
}
