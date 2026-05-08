package com.sergeev.taskmanager.company.internal.repository;

import com.sergeev.taskmanager.company.internal.entity.CompanyRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRoleRepository extends JpaRepository<CompanyRole, Long> {

    Optional<CompanyRole> findByCompanyIdAndName(Long companyId, String name);

    boolean existsByCompanyIdAndName(Long companyId, String name);

    List<CompanyRole> findAllByCompanyId(Long companyId);

    Optional<CompanyRole> findByCompanyIdAndId(Long companyId, Long id);
}
