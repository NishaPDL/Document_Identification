package com.nishapdl.documentidentification.util;

/**
 * Application constants for Document Identification API.
 */
public final class Constants {

    private Constants() {
        // Utility class - prevent instantiation
    }

    // API Endpoints
    public static final String CLASSIFY_DOCUMENTS_ENDPOINT = "/classify-documents";

    // Document Types
    public static final String DOCUMENT_TYPE_AADHAAR = "Aadhaar";
    public static final String DOCUMENT_TYPE_PAN = "PAN";
    public static final String DOCUMENT_TYPE_VOTER_ID = "Voter ID";
    public static final String DOCUMENT_TYPE_DRIVING_LICENSE = "Driving License";
    public static final String DOCUMENT_TYPE_NONE = "None";

    // File Types
    public static final String ZIP_FILE_EXTENSION = ".zip";

    // Error Messages
    public static final String ERROR_INVALID_FILE_TYPE = "Only ZIP files are allowed.";
    public static final String ERROR_NO_IMAGES_FOUND = "No image files found in ZIP.";
    public static final String ERROR_OCR_FAILED = "OCR failed: %s";
    public static final String ERROR_CLASSIFICATION_FAILED = "Document classification failed: %s";
    public static final String ERROR_FILE_PROCESSING_FAILED = "File processing failed: %s";

    // Gemini Prompt Template
    public static final String GEMINI_CLASSIFICATION_PROMPT = """
            You are an assistant that classifies Indian identity documents based on OCR-extracted text.
            
            Respond with only ONE of the following:
            - Aadhaar
            - PAN
            - Voter ID
            - Driving License
            - None
            
            Here is the extracted text:
            %s
            
            What type of document is this? Respond with only one word.
            """;
}
