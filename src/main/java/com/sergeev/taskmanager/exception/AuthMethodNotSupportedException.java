package com.sergeev.taskmanager.exception;

import org.springframework.security.authentication.AuthenticationServiceException;

public class AuthMethodNotSupportedException extends AuthenticationServiceException {
    public AuthMethodNotSupportedException(final String msg) {
        super(msg);
    }
}
