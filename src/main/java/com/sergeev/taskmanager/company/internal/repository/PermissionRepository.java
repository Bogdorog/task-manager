package com.sergeev.taskmanager.company.internal.repository;

import com.sergeev.taskmanager.company.internal.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);
}