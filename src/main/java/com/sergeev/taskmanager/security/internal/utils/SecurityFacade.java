package com.sergeev.taskmanager.security.internal.utils;

import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class SecurityFacade implements SecurityFacadeApi {

    public Long getCurrentUserId() {
        return ((Jwt)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal()
        ).getClaim("id");
    }
}
