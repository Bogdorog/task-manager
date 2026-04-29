package com.sergeev.taskmanager.user.internal.repository;

import com.sergeev.taskmanager.user.internal.entity.User;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByLogin(String login);

    @NonNull Optional<User> findById(Long id);
}
