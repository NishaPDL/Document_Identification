package com.nishapdl.documentidentification.controller;

import com.nishapdl.documentidentification.dto.ClassificationResponse;
import com.nishapdl.documentidentification.service.DocumentClassificationService;
import com.nishapdl.documentidentification.service.FileProcessingService;
import com.nishapdl.documentidentification.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for document classification operations.
 * Provides endpoints for uploading ZIP files containing document images
 * and returning classification results.
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DocumentClassificationController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentClassificationController.class);

    private final FileProcessingService fileProcessingService;
    private final DocumentClassificationService documentClassificationService;

    @Autowired
    public DocumentClassificationController(FileProcessingService fileProcessingService,
                                          DocumentClassificationService documentClassificationService) {
        this.fileProcessingService = fileProcessingService;
        this.documentClassificationService = documentClassificationService;
    }

    /**
     * Classifies documents from uploaded ZIP file.
     * 
     * @param file ZIP file containing document images
     * @return ResponseEntity with classification results
     */
    @PostMapping(value = Constants.CLASSIFY_DOCUMENTS_ENDPOINT, 
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> classifyDocuments(
            @RequestParam("file") MultipartFile file) {
        
        logger.info("Received document classification request for file: {}", file.getOriginalFilename());
        
        try {
            // Validate ZIP file
            fileProcessingService.validateZipFile(file);
            
            // Extract images from ZIP
            List<Path> imagePaths = fileProcessingService.extractImagesFromZip(file);
            
            // Classify documents
            Map<String, String> results = documentClassificationService.classifyDocuments(imagePaths);
            
            logger.info("Successfully processed {} documents from file: {}", 
                       results.size(), file.getOriginalFilename());
            
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Failed to process document classification request for file: {}", 
                        file.getOriginalFilename(), e);
            throw e; // Let the global exception handler deal with it
        }
    }

    /**
     * Health check endpoint.
     * 
     * @return ResponseEntity with health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Document Identification API",
            "version", "1.0.0"
        ));
    }

    /**
     * Get supported document types.
     * 
     * @return ResponseEntity with supported document types
     */
    @GetMapping("/document-types")
    public ResponseEntity<Map<String, Object>> getSupportedDocumentTypes() {
        return ResponseEntity.ok(Map.of(
            "supportedTypes", List.of(
                Constants.DOCUMENT_TYPE_AADHAAR,
                Constants.DOCUMENT_TYPE_PAN,
                Constants.DOCUMENT_TYPE_VOTER_ID,
                Constants.DOCUMENT_TYPE_DRIVING_LICENSE,
                Constants.DOCUMENT_TYPE_NONE
            ),
            "supportedImageFormats", fileProcessingService.getSupportedImageExtensions()
        ));
    }
}
