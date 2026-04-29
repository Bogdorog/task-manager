package com.sergeev.taskmanager.user.api;

import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CustomUserDetailsService extends UserDetailsService {
    @NonNull UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException;
    UserDetails loadUserById(Long id) throws UsernameNotFoundException;
    Long getId(UserDetails user);
}
