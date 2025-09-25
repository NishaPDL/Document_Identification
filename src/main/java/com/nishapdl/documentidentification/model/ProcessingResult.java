package com.nishapdl.documentidentification.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Represents the result of document processing operation.
 * Contains classification results, metadata, and processing statistics.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Document processing result with classification and metadata")
public class ProcessingResult {

    @Schema(description = "Map of filename to document type classification", example = "{\"document1.jpg\": \"PAN\", \"document2.png\": \"Aadhaar\"}")
    private Map<String, String> classifications;

    @Schema(description = "Processing metadata and statistics")
    private ProcessingMetadata metadata;

    @Schema(description = "Processing timestamp")
    private LocalDateTime processedAt;

    @Schema(description = "Processing status", example = "SUCCESS")
    private ProcessingStatus status;

    @Schema(description = "Error message if processing failed")
    private String errorMessage;

    public ProcessingResult() {
        this.processedAt = LocalDateTime.now();
        this.status = ProcessingStatus.SUCCESS;
    }

    public ProcessingResult(Map<String, String> classifications, ProcessingMetadata metadata) {
        this();
        this.classifications = classifications;
        this.metadata = metadata;
    }

    public static ProcessingResult success(Map<String, String> classifications, ProcessingMetadata metadata) {
        return new ProcessingResult(classifications, metadata);
    }

    public static ProcessingResult error(String errorMessage) {
        ProcessingResult result = new ProcessingResult();
        result.status = ProcessingStatus.ERROR;
        result.errorMessage = errorMessage;
        return result;
    }

    // Getters and Setters
    public Map<String, String> getClassifications() {
        return classifications;
    }

    public void setClassifications(Map<String, String> classifications) {
        this.classifications = classifications;
    }

    public ProcessingMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ProcessingMetadata metadata) {
        this.metadata = metadata;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Processing status enumeration
     */
    public enum ProcessingStatus {
        SUCCESS,
        PARTIAL_SUCCESS,
        ERROR
    }

    /**
     * Processing metadata containing statistics and performance metrics
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Processing metadata and performance statistics")
    public static class ProcessingMetadata {
        
        @Schema(description = "Total number of files processed", example = "5")
        private int totalFiles;
        
        @Schema(description = "Number of successfully processed files", example = "4")
        private int successfulFiles;
        
        @Schema(description = "Number of failed files", example = "1")
        private int failedFiles;
        
        @Schema(description = "Total processing time in milliseconds", example = "2500")
        private long processingTimeMs;
        
        @Schema(description = "Average processing time per file in milliseconds", example = "500")
        private double avgProcessingTimeMs;
        
        @Schema(description = "Original ZIP file size in bytes", example = "1048576")
        private long originalFileSizeBytes;
        
        @Schema(description = "Number of images extracted from ZIP", example = "5")
        private int extractedImages;

        public ProcessingMetadata() {}

        public ProcessingMetadata(int totalFiles, int successfulFiles, int failedFiles, 
                                long processingTimeMs, long originalFileSizeBytes, int extractedImages) {
            this.totalFiles = totalFiles;
            this.successfulFiles = successfulFiles;
            this.failedFiles = failedFiles;
            this.processingTimeMs = processingTimeMs;
            this.avgProcessingTimeMs = totalFiles > 0 ? (double) processingTimeMs / totalFiles : 0;
            this.originalFileSizeBytes = originalFileSizeBytes;
            this.extractedImages = extractedImages;
        }

        // Getters and Setters
        public int getTotalFiles() {
            return totalFiles;
        }

        public void setTotalFiles(int totalFiles) {
            this.totalFiles = totalFiles;
        }

        public int getSuccessfulFiles() {
            return successfulFiles;
        }

        public void setSuccessfulFiles(int successfulFiles) {
            this.successfulFiles = successfulFiles;
        }

        public int getFailedFiles() {
            return failedFiles;
        }

        public void setFailedFiles(int failedFiles) {
            this.failedFiles = failedFiles;
        }

        public long getProcessingTimeMs() {
            return processingTimeMs;
        }

        public void setProcessingTimeMs(long processingTimeMs) {
            this.processingTimeMs = processingTimeMs;
        }

        public double getAvgProcessingTimeMs() {
            return avgProcessingTimeMs;
        }

        public void setAvgProcessingTimeMs(double avgProcessingTimeMs) {
            this.avgProcessingTimeMs = avgProcessingTimeMs;
        }

        public long getOriginalFileSizeBytes() {
            return originalFileSizeBytes;
        }

        public void setOriginalFileSizeBytes(long originalFileSizeBytes) {
            this.originalFileSizeBytes = originalFileSizeBytes;
        }

        public int getExtractedImages() {
            return extractedImages;
        }

        public void setExtractedImages(int extractedImages) {
            this.extractedImages = extractedImages;
        }
    }
}
