package com.sergeev.taskmanager.company.internal.repository;

import com.sergeev.taskmanager.company.api.PermissionEnum;
import com.sergeev.taskmanager.company.internal.entity.CompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {

    boolean existsByUserIdAndCompanyId(Long userId, Long companyId);

    Optional<CompanyMembership> findByUserIdAndCompanyId(Long userId, Long companyId);

    List<CompanyMembership> findAllByCompanyId(Long companyId);

    boolean existsByRoleId(Long roleId);

    List<CompanyMembership> findAllByUserId(Long userId);

    //TODO Добовить кэширование на этот запрос
    @Query("""
    SELECT m.userId
    FROM CompanyMembership m
    JOIN m.role r
    JOIN r.permissions p
    WHERE p.name = :#{#permission.title}
      AND m.company.id = :companyId
""")
    List<Long> findUserIdsByPermissionAndCompany(
            @Param("permission") PermissionEnum permission,
            @Param("companyId") Long companyId
    );
}
