package com.sergeev.taskmanager.user.internal.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AccountDeleteService {

    public String generateToken() {
        int random = new SecureRandom().nextInt(100000,999999);
        return Integer.toString(random);
    }
}
