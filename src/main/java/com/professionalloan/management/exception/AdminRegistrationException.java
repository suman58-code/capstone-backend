package com.professionalloan.management.exception;

/**
 * Exception thrown when non-admin users attempt to register.
 */
public class AdminRegistrationException extends RuntimeException {
    public AdminRegistrationException(String message) {
        super(message);
    }
} 