package com.professionalloan.management.exception;

/**
 * Exception thrown when there is an error in payment processing.
 */
public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
    
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
} 