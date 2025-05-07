package com.professionalloan.management.exception;

/**
 * Exception thrown when a document is not found in the system.
 */
public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException(String message) {
        super(message);
    }
    
    public DocumentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
