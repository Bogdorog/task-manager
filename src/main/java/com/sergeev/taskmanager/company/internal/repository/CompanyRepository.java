package com.sergeev.taskmanager.company.internal.repository;

import com.sergeev.taskmanager.company.internal.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
