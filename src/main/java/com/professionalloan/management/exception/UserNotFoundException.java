package com.professionalloan.management.exception;

/**
 * Exception thrown when a user is not found in the system.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
