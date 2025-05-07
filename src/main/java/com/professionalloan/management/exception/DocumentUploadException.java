package com.professionalloan.management.exception;

/**
 * Exception thrown when there is an error uploading or processing a document.
 */
public class DocumentUploadException extends RuntimeException {
    public DocumentUploadException(String message) {
        super(message);
    }
    
    public DocumentUploadException(String message, Throwable cause) {
        super(message, cause);
    }
} 