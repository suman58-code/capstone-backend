package com.professionalloan.management.exception;

/**
 * Exception thrown when an invalid document type is provided.
 */
public class InvalidDocumentTypeException extends RuntimeException {
    public InvalidDocumentTypeException(String message) {
        super(message);
    }
}
