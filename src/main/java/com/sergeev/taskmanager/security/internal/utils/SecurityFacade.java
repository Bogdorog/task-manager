package com.sergeev.taskmanager.security.internal.utils;

import com.sergeev.taskmanager.security.api.SecurityFacadeApi;
import com.sergeev.taskmanager.user.internal.entity.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityFacade implements SecurityFacadeApi {

    public Long getCurrentUserId() {
        return ((UserDetailsImpl)
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal()
        ).getId();
    }
}
