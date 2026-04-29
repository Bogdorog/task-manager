package com.sergeev.taskmanager.user.internal.service;

import com.sergeev.taskmanager.user.api.CustomUserDetailsService;
import com.sergeev.taskmanager.user.internal.entity.User;
import com.sergeev.taskmanager.user.internal.entity.UserDetailsImpl;
import com.sergeev.taskmanager.user.internal.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements CustomUserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public @NonNull UserDetails loadUserByUsername(final @NonNull String login) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Не существует пользователя с логином: " + login));
        return UserDetailsImpl.build(user);
    }

    @Transactional
    public UserDetails loadUserById(final Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Не существует пользователя с id: " + id));
        return UserDetailsImpl.build(user);
    }

    public Long getId(UserDetails user) {
        return ((UserDetailsImpl) user).getId();
    }

    @Bean
    @Primary
    CustomUserDetailsService customUserDetailsService() {
        return new UserDetailsServiceImpl(userRepository);
    }
}
