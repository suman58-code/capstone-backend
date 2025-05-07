package com.professionalloan.management.exception;

/**
 * Exception thrown when a repayment (EMI) is not found in the system.
 */
public class RepaymentNotFoundException extends RuntimeException {
    public RepaymentNotFoundException(String message) {
        super(message);
    }
} 