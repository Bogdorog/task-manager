package com.sergeev.taskmanager.user.internal.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AccountDeleteService {

    public String generateToken() {
        Integer random = new SecureRandom().nextInt(100000,999999);
        return random.toString();
    }
}
