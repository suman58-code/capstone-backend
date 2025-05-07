package com.professionalloan.management.exception;

/**
 * Exception thrown when a duplicate loan application is detected.
 */
public class DuplicateLoanApplicationException extends RuntimeException {
    public DuplicateLoanApplicationException(String message) {
        super(message);
    }
}
