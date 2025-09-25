package com.nishapdl.documentidentification.controller;

import com.nishapdl.documentidentification.model.DocumentType;
import com.nishapdl.documentidentification.model.ProcessingResult;
import com.nishapdl.documentidentification.service.DocumentClassificationService;
import com.nishapdl.documentidentification.service.FileProcessingService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Enhanced REST Controller for document classification operations.
 * 
 * Provides comprehensive endpoints for document processing with:
 * - Synchronous and asynchronous processing
 * - Detailed metrics and monitoring
 * - OpenAPI documentation
 * - Comprehensive error handling
 */
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Document Classification", description = "APIs for classifying Indian identity documents")
public class DocumentClassificationController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentClassificationController.class);

    private final DocumentClassificationService documentClassificationService;
    private final FileProcessingService fileProcessingService;
    private final Counter requestCounter;
    private final Counter successCounter;
    private final Counter errorCounter;

    @Autowired
    public DocumentClassificationController(
            DocumentClassificationService documentClassificationService,
            FileProcessingService fileProcessingService,
            MeterRegistry meterRegistry) {
        this.documentClassificationService = documentClassificationService;
        this.fileProcessingService = fileProcessingService;
        this.requestCounter = Counter.builder("document_classification_requests_total")
                .description("Total number of document classification requests")
                .register(meterRegistry);
        this.successCounter = Counter.builder("document_classification_success_total")
                .description("Total number of successful document classifications")
                .register(meterRegistry);
        this.errorCounter = Counter.builder("document_classification_errors_total")
                .description("Total number of document classification errors")
                .register(meterRegistry);
    }

    /**
     * Synchronous document classification endpoint.
     * Processes ZIP file containing document images and returns classification results.
     */
    @PostMapping(value = "/classify-documents", 
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Classify documents from ZIP file",
        description = "Upload a ZIP file containing document images and get classification results for each document"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Documents classified successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProcessingResult.class),
                examples = @ExampleObject(
                    value = "{\"classifications\": {\"document1.jpg\": \"PAN\", \"document2.png\": \"Aadhaar\"}, \"metadata\": {\"totalFiles\": 2, \"successfulFiles\": 2, \"failedFiles\": 0, \"processingTimeMs\": 1500}}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Invalid file or request"),
        @ApiResponse(responseCode = "413", description = "File too large"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Timed(value = "document_classification_duration", description = "Time taken to classify documents")
    public ResponseEntity<ProcessingResult> classifyDocuments(
            @Parameter(description = "ZIP file containing document images", required = true)
            @RequestParam("file") @NotNull MultipartFile file) {
        
        logger.info("Received synchronous document classification request for file: {} (size: {} bytes)", 
                   file.getOriginalFilename(), file.getSize());
        
        requestCounter.increment();
        
        try {
            ProcessingResult result = documentClassificationService.processDocuments(file);
            successCounter.increment();
            
            logger.info("Successfully processed {} documents from file: {}", 
                       result.getClassifications().size(), file.getOriginalFilename());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            errorCounter.increment();
            logger.error("Failed to process document classification request for file: {}", 
                        file.getOriginalFilename(), e);
            throw e;
        }
    }

    /**
     * Asynchronous document classification endpoint.
     * Processes ZIP file asynchronously and returns processing result.
     */
    @PostMapping(value = "/classify-documents-async", 
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Classify documents asynchronously",
        description = "Upload a ZIP file for asynchronous document classification processing"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Documents classified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Timed(value = "document_classification_async_duration", description = "Time taken for async document classification")
    public CompletableFuture<ResponseEntity<ProcessingResult>> classifyDocumentsAsync(
            @Parameter(description = "ZIP file containing document images", required = true)
            @RequestParam("file") @NotNull MultipartFile file) {
        
        logger.info("Received asynchronous document classification request for file: {} (size: {} bytes)", 
                   file.getOriginalFilename(), file.getSize());
        
        requestCounter.increment();
        
        return documentClassificationService.processDocumentsAsync(file)
                .thenApply(result -> {
                    successCounter.increment();
                    logger.info("Successfully processed {} documents asynchronously from file: {}", 
                               result.getClassifications().size(), file.getOriginalFilename());
                    return ResponseEntity.ok(result);
                })
                .exceptionally(throwable -> {
                    errorCounter.increment();
                    logger.error("Failed to process async document classification request for file: {}", 
                                file.getOriginalFilename(), throwable);
                    return ResponseEntity.internalServerError()
                            .body(ProcessingResult.error("Async processing failed: " + throwable.getMessage()));
                });
    }

    /**
     * Health check endpoint with detailed status information.
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Check the health status of the document classification service"
    )
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Enhanced Document Identification API",
            "version", "2.0.0",
            "timestamp", System.currentTimeMillis(),
            "features", List.of("sync-processing", "async-processing", "caching", "metrics")
        ));
    }

    /**
     * Get comprehensive information about supported document types and formats.
     */
    @GetMapping("/document-types")
    @Operation(
        summary = "Get supported document types",
        description = "Retrieve information about supported document types and image formats"
    )
    @ApiResponse(responseCode = "200", description = "Document types retrieved successfully")
    public ResponseEntity<Map<String, Object>> getSupportedDocumentTypes() {
        List<Map<String, String>> documentTypes = Arrays.stream(DocumentType.values())
                .map(type -> Map.of(
                    "code", type.getCode(),
                    "displayName", type.getDisplayName(),
                    "description", type.getDescription(),
                    "isValid", String.valueOf(type.isValid()),
                    "isGovernmentIssued", String.valueOf(type.isGovernmentIssued())
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "supportedDocumentTypes", documentTypes,
            "supportedImageFormats", fileProcessingService.getSupportedImageExtensions(),
            "maxFileSize", "100MB",
            "processingFeatures", List.of(
                "OCR with Google Cloud Vision",
                "AI Classification with Vertex AI Gemini",
                "Batch processing",
                "Async processing",
                "Caching support"
            )
        ));
    }

    /**
     * Get processing statistics and metrics.
     */
    @GetMapping("/stats")
    @Operation(
        summary = "Get processing statistics",
        description = "Retrieve processing statistics and performance metrics"
    )
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    public ResponseEntity<Map<String, Object>> getProcessingStats() {
        return ResponseEntity.ok(Map.of(
            "totalRequests", requestCounter.count(),
            "successfulRequests", successCounter.count(),
            "failedRequests", errorCounter.count(),
            "successRate", requestCounter.count() > 0 ? 
                (successCounter.count() / requestCounter.count()) * 100 : 0,
            "supportedDocumentTypes", DocumentType.values().length - 1, // Exclude NONE
            "cacheEnabled", true,
            "asyncProcessingEnabled", true
        ));
    }

    /**
     * Validate a single document type classification.
     */
    @PostMapping("/validate-classification")
    @Operation(
        summary = "Validate document classification",
        description = "Validate if a given classification result is correct for a document type"
    )
    @ApiResponse(responseCode = "200", description = "Validation completed")
    public ResponseEntity<Map<String, Object>> validateClassification(
            @Parameter(description = "Document type to validate", required = true)
            @RequestParam("documentType") String documentType,
            @Parameter(description = "Expected classification result", required = true)
            @RequestParam("expectedType") String expectedType) {
        
        DocumentType docType = DocumentType.fromString(documentType);
        DocumentType expectedDocType = DocumentType.fromString(expectedType);
        
        boolean isValid = docType == expectedDocType && docType.isValid();
        
        return ResponseEntity.ok(Map.of(
            "isValid", isValid,
            "documentType", docType.getDisplayName(),
            "expectedType", expectedDocType.getDisplayName(),
            "confidence", isValid ? 1.0 : 0.0,
            "recommendation", isValid ? "Classification is correct" : 
                "Classification mismatch - please review the document"
        ));
    }
}
