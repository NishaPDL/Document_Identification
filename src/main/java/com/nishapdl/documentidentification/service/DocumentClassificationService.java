package com.nishapdl.documentidentification.service;

import com.nishapdl.documentidentification.model.ProcessingResult;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced service interface for document classification operations.
 * 
 * Provides both synchronous and asynchronous processing capabilities
 * with comprehensive error handling and performance monitoring.
 */
public interface DocumentClassificationService {

    /**
     * Process documents synchronously from a multipart file.
     * 
     * @param file ZIP file containing document images
     * @return ProcessingResult with classifications and metadata
     */
    ProcessingResult processDocuments(MultipartFile file);

    /**
     * Process documents asynchronously from a multipart file.
     * 
     * @param file ZIP file containing document images
     * @return CompletableFuture with ProcessingResult
     */
    CompletableFuture<ProcessingResult> processDocumentsAsync(MultipartFile file);

    /**
     * Classify individual documents from extracted image paths.
     * 
     * @param imagePaths List of paths to image files
     * @return Map of filename to document type classification
     */
    Map<String, String> classifyDocuments(List<Path> imagePaths);

    /**
     * Classify a single document image.
     * 
     * @param imagePath Path to the image file
     * @return Document type classification
     */
    String classifyDocument(Path imagePath);

    /**
     * Clear classification cache.
     */
    void clearCache();

    /**
     * Get cache statistics.
     * 
     * @return Map containing cache statistics
     */
    Map<String, Object> getCacheStats();
}
