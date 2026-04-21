package com.sergeev.taskmanager.user.internal.repository;

import com.sergeev.taskmanager.user.internal.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
       delete from PasswordResetToken t
       where t.used = true
          or t.expiresAt < :now
       """)
    void deleteExpiredOrUsed(LocalDateTime now);

    @Modifying
    @Query("""
       update PasswordResetToken t
       set t.used = true
       where t.userId = :userId
       """)
    void invalidateAllForUser(Long userId);
}