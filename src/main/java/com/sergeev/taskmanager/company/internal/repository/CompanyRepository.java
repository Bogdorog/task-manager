package com.sergeev.taskmanager.company.internal.repository;

import com.sergeev.taskmanager.company.internal.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query(value = """
            SELECT c.name
            FROM companies c
            WHERE c.id = :company_id
            """, nativeQuery = true)
    String findCompanyNameById(Long company_id);
}
