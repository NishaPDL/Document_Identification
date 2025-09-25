package com.nishapdl.documentidentification.exception;

/**
 * Custom exception for document processing errors.
 * This exception is thrown when errors occur during document classification,
 * OCR processing, or file handling operations.
 */
public class DocumentProcessingException extends RuntimeException {

    public DocumentProcessingException(String message) {
        super(message);
    }

    public DocumentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentProcessingException(Throwable cause) {
        super(cause);
    }
}
