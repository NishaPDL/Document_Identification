package com.nishapdl.documentidentification;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enhanced Document Identification API - Spring Boot Application
 * 
 * This application provides advanced document classification capabilities for Indian identity documents
 * using Google Cloud Vision API for OCR and Vertex AI Gemini for intelligent classification.
 * 
 * Features:
 * - Asynchronous processing for better performance
 * - Caching for improved response times
 * - Comprehensive monitoring and metrics
 * - OpenAPI documentation
 * - Production-ready configuration
 * 
 * @author NishaPDL
 * @version 2.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@OpenAPIDefinition(
    info = @Info(
        title = "Enhanced Document Identification API",
        version = "2.0.0",
        description = "Advanced API for classifying Indian identity documents using AI/ML technologies",
        contact = @Contact(
            name = "NishaPDL",
            email = "pythonai@paisalo.in",
            url = "https://github.com/NishaPDL"
        ),
        license = @License(
            name = "MIT License",
            url = "https://opensource.org/licenses/MIT"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Development Server"),
        @Server(url = "https://api.nishapdl.com", description = "Production Server")
    }
)
public class DocumentIdentificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentIdentificationApplication.class, args);
    }
}
