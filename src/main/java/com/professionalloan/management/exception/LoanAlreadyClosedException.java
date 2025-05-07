package com.professionalloan.management.exception;

/**
 * Exception thrown when an operation is attempted on a closed loan.
 */
public class LoanAlreadyClosedException extends RuntimeException {
    public LoanAlreadyClosedException(String message) {
        super(message);
    }
}
