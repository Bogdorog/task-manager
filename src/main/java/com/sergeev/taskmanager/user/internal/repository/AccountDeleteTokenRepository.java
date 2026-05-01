package com.sergeev.taskmanager.user.internal.repository;

import com.sergeev.taskmanager.user.internal.entity.AccountDeleteToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountDeleteTokenRepository extends JpaRepository<AccountDeleteToken, Long> {

    Optional<AccountDeleteToken> findByToken(String tokenHash);
}
