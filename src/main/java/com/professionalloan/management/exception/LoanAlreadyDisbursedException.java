package com.professionalloan.management.exception;

/**
 * Exception thrown when attempting to disburse a loan that has already been disbursed.
 */
public class LoanAlreadyDisbursedException extends RuntimeException {
    public LoanAlreadyDisbursedException(String message) {
        super(message);
    }
} 