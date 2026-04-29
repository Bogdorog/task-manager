package com.sergeev.taskmanager.security.internal.login;

import com.sergeev.taskmanager.user.api.CustomUserDetailsService;
import io.github.resilience4j.core.lang.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
public class LoginAuthenticationProvider implements AuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public LoginAuthenticationProvider(@Qualifier("customUserDetailsService") final CustomUserDetailsService userDetailsService) {
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(@NonNull final Authentication authentication)
            throws AuthenticationException {

        String username = (String) authentication.getPrincipal();
        String password = Objects.requireNonNull(authentication.getCredentials()).toString();

        UserDetails userDetails = userDetailsService.loadUserByUsername(Objects.requireNonNull(username));

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            log.debug("Аутентификация провалилась: неправильный логин или пароль '{}'", username);
            throw new BadCredentialsException("Неправильный логин или пароль");
        }

        log.debug("Успешный вход для пользователя '{}'", username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(@NonNull final Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
