package com.sergeev.taskmanager.user.internal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;
    private final Long id;
    private final String username;
    private final String email;
    private final boolean active;
    @JsonIgnore
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(final User user, final Collection<? extends GrantedAuthority> authorities) {
        this.id = user.getId();
        this.username = user.getLogin();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();
        this.authorities = authorities;
        this.active = user.isActive();
    }


    public static UserDetailsImpl build(User user) {
        return new UserDetailsImpl(user, buildGrantedAuthorities(user));
    }

    private static List<GrantedAuthority> buildGrantedAuthorities(final User user) {
        Role role = user.getRole();
        if (role == null) {
            return List.of();
        }
        return List.of(
                new SimpleGrantedAuthority("ROLE_" + role.getName())
        );
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
