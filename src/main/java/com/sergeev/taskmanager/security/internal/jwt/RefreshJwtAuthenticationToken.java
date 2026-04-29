package com.sergeev.taskmanager.security.internal.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

public class RefreshJwtAuthenticationToken extends AbstractAuthenticationToken {

    @Getter
    private final String refreshToken;
    private final Object principal;

    // До аутентификации — только неаутентифицированный токен
    public RefreshJwtAuthenticationToken(String refreshToken) {
        super(Collections.emptyList());
        this.refreshToken = refreshToken;
        this.principal = null;
        setAuthenticated(false); // токен не аутентифицирован
    }

    // После аутентификации — с principal и authorities
    public RefreshJwtAuthenticationToken(UserDetails userDetails) {
        super(userDetails.getAuthorities());
        this.refreshToken = null;
        this.principal = userDetails;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return refreshToken;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

}
