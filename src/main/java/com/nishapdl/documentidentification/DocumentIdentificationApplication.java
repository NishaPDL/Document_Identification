package com.nishapdl.documentidentification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Document Identification API.
 * This application classifies Indian identity documents using Google Cloud Vision API for OCR
 * and Vertex AI Gemini model for document type classification.
 */
@SpringBootApplication
public class DocumentIdentificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentIdentificationApplication.class, args);
    }
}
