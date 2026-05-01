package com.sergeev.taskmanager.user.internal.repository;

import com.sergeev.taskmanager.user.internal.entity.Role;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @NonNull Optional<Role> findById(Long id);

    Optional<Role> findByName(String name);
}
