package com.professionalloan.management.exception;

/**
 * Exception thrown when attempting to pay an EMI that has already been paid.
 */
public class EMIAlreadyPaidException extends RuntimeException {
    public EMIAlreadyPaidException(String message) {
        super(message);
    }
}
