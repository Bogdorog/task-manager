package com.sergeev.taskmanager.user.internal.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasswordChangeService {
    public String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(bytes);
    }

    public String hashToken(String token) {
        //return DigestUtils.sha256Hex(token);
        return new DigestUtils("SHA3-256").digestAsHex(token);
    }
}
