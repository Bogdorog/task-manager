package com.sergeev.taskmanager.security.internal.jwt;

import com.sergeev.taskmanager.exception.InvalidRefreshTokenException;
import com.sergeev.taskmanager.security.internal.jwt.entity.RefreshToken;
import com.sergeev.taskmanager.security.internal.jwt.repository.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final SecretKey jwtSecretKey;

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.tokenExpirationTime}")
    private int tokenExpirationInSec;

    @Value("${security.jwt.refreshTokenExpirationTime}")
    private int refreshTokenExpirationInSec;


    public String generateAccessToken(UserDetails userDetails) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(tokenExpirationInSec);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(jwtSecretKey, Jwts.SIG.HS512)
                .compact();
    }

    @Transactional
    public String generateRefreshToken(Long userId) {
        // Удаляем старый refresh токен если есть
        refreshTokenRepository.deleteByUserId(userId);
        refreshTokenRepository.flush();

        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUserId(userId);
        refreshToken.setExpiresAt(
                LocalDateTime.now().plusSeconds(refreshTokenExpirationInSec)
        );

        refreshTokenRepository.save(refreshToken);

        log.debug("Создан токен обновления для пользователя {}", userId);
        return tokenValue;
    }

    @Transactional
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not found"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new InvalidRefreshTokenException("Refresh token expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
        log.info("Refresh token revoked for user {}", userId);
    }
}
