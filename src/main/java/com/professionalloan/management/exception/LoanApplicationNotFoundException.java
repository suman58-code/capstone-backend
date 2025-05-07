 package com.professionalloan.management.exception;

/**
 * Exception thrown when a loan application is not found.
 */
public class LoanApplicationNotFoundException extends RuntimeException {
    public LoanApplicationNotFoundException(String message) {
        super(message);
    }
}

