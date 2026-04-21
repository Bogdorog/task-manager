package com.sergeev.taskmanager.user.api;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CustomUserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    UserDetails loadUserById(Long id) throws UsernameNotFoundException;
    public Long getId(UserDetails user);
}
