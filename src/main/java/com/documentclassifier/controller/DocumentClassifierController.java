package com.documentclassifier.controller;

import com.documentclassifier.dto.ClassificationResult;
import com.documentclassifier.service.DocumentClassificationService;
import com.documentclassifier.service.DocumentUploadService;
import com.documentclassifier.service.FileProcessingService;
import com.documentclassifier.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DocumentClassifierController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentClassifierController.class);
    
    private final FileProcessingService fileProcessingService;
    private final OcrService ocrService;
    private final DocumentClassificationService classificationService;
    private final DocumentUploadService uploadService;
    
    @Autowired
    public DocumentClassifierController(
            FileProcessingService fileProcessingService,
            OcrService ocrService,
            DocumentClassificationService classificationService,
            DocumentUploadService uploadService) {
        this.fileProcessingService = fileProcessingService;
        this.ocrService = ocrService;
        this.classificationService = classificationService;
        this.uploadService = uploadService;
    }
    
    @PostMapping("/classify-documents")
    public ResponseEntity<?> classifyDocuments(@RequestParam("file") MultipartFile file) {
        logger.info("Received document classification request for file: {}", file.getOriginalFilename());
        
        // Validate file type
        if (!isZipFile(file)) {
            logger.warn("Invalid file type received: {}", file.getContentType());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only ZIP files are allowed."));
        }
        
        List<File> extractedFiles = null;
        
        try {
            // Extract images from ZIP
            extractedFiles = fileProcessingService.extractImagesFromZip(file);
            logger.info("Extracted {} images from ZIP file", extractedFiles.size());
            
            // Process each image
            Map<String, String> results = new HashMap<>();
            
            for (File imageFile : extractedFiles) {
                String filename = imageFile.getName();
                
                try {
                    // Extract text using OCR
                    String extractedText = ocrService.extractTextFromImage(imageFile);
                    
                    if (extractedText.isEmpty()) {
                        results.put(filename, "None");
                        logger.warn("No text extracted from image: {}", filename);
                        continue;
                    }
                    
                    // Classify document type
                    String documentType = classificationService.classifyDocumentType(extractedText);
                    results.put(filename, documentType);
                    
                    logger.debug("Processed {}: {}", filename, documentType);
                    
                } catch (Exception e) {
                    logger.error("Error processing image {}: {}", filename, e.getMessage());
                    results.put(filename, "Error: " + e.getMessage());
                }
            }
            
            logger.info("Successfully processed {} images", results.size());
            return ResponseEntity.ok(results);
            
        } catch (Exception e) {
            logger.error("Error processing ZIP file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
            
        } finally {
            // Clean up temporary files
            if (extractedFiles != null) {
                fileProcessingService.cleanupFiles(extractedFiles);
            }
        }
    }
    
    @PostMapping("/classify-and-upload")
    public ResponseEntity<?> classifyAndUploadDocuments(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId) {
        
        logger.info("Received classify and upload request for user: {} with file: {}", userId, file.getOriginalFilename());
        
        // Validate file type
        if (!isZipFile(file)) {
            logger.warn("Invalid file type received: {}", file.getContentType());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only ZIP files are allowed."));
        }
        
        // Validate userId
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User ID is required."));
        }
        
        Map<String, File> extractedFiles = null;
        
        try {
            // Extract images from ZIP as a map
            extractedFiles = fileProcessingService.extractImagesFromZipAsMap(file);
            logger.info("Extracted {} images from ZIP file", extractedFiles.size());
            
            // Process each image for classification
            Map<String, String> classificationResults = new HashMap<>();
            
            for (Map.Entry<String, File> entry : extractedFiles.entrySet()) {
                String filename = entry.getKey();
                File imageFile = entry.getValue();
                
                try {
                    // Extract text using OCR
                    String extractedText = ocrService.extractTextFromImage(imageFile);
                    
                    if (extractedText.isEmpty()) {
                        classificationResults.put(filename, "None");
                        logger.warn("No text extracted from image: {}", filename);
                        continue;
                    }
                    
                    // Classify document type
                    String documentType = classificationService.classifyDocumentType(extractedText);
                    classificationResults.put(filename, documentType);
                    
                    logger.debug("Processed {}: {}", filename, documentType);
                    
                } catch (Exception e) {
                    logger.error("Error processing image {}: {}", filename, e.getMessage());
                    classificationResults.put(filename, "Error: " + e.getMessage());
                }
            }
            
            // Upload classified documents
            Map<String, Object> uploadResults = uploadService.uploadClassifiedDocuments(
                    classificationResults, extractedFiles, userId);
            
            logger.info("Successfully processed and uploaded {} documents for user {}", 
                    uploadResults.size(), userId);
            
            // Return combined results
            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalFiles", extractedFiles.size());
            response.put("results", uploadResults);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing ZIP file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
            
        } finally {
            // Clean up temporary files
            if (extractedFiles != null) {
                uploadService.cleanupTempFiles(extractedFiles);
            }
        }
    }
    
    @PostMapping("/classify-and-upload-single")
    public ResponseEntity<?> classifyAndUploadSingleDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId) {
        
        logger.info("Received single document upload request for user: {} with file: {}", userId, file.getOriginalFilename());
        
        // Validate file type
        if (!isImageFile(file)) {
            logger.warn("Invalid file type received: {}", file.getContentType());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Only image files are allowed (PNG, JPG, JPEG, WebP, BMP)."));
        }
        
        // Validate userId
        if (userId == null || userId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "User ID is required."));
        }
        
        File tempFile = null;
        
        try {
            // Save uploaded file temporarily
            tempFile = saveUploadedFileTemporarily(file);
            logger.info("Saved temporary file: {}", tempFile.getPath());
            
            // Extract text using OCR
            String extractedText = ocrService.extractTextFromImage(tempFile);
            
            if (extractedText.isEmpty()) {
                logger.warn("No text extracted from image: {}", file.getOriginalFilename());
                return ResponseEntity.ok(Map.of(
                    "classification", "None",
                    "uploaded", false,
                    "reason", "No text could be extracted from the image"
                ));
            }
            
            // Classify document type
            String documentType = classificationService.classifyDocumentType(extractedText);
            logger.debug("Document classified as: {}", documentType);
            
            // Check if classification was successful
            if (documentType.startsWith("Error") || documentType.equals("None")) {
                return ResponseEntity.ok(Map.of(
                    "classification", documentType,
                    "uploaded", false,
                    "reason", documentType.startsWith("Error") ? "Classification error" : "Document type not recognized"
                ));
            }
            
            // Upload the classified document
            Map<String, String> classificationResults = Map.of(file.getOriginalFilename(), documentType);
            Map<String, File> imageFiles = Map.of(file.getOriginalFilename(), tempFile);
            
            Map<String, Object> uploadResults = uploadService.uploadClassifiedDocuments(
                    classificationResults, imageFiles, userId);
            
            // Get the result for this specific file
            Map<String, Object> fileResult = (Map<String, Object>) uploadResults.get(file.getOriginalFilename());
            
            logger.info("Successfully processed and uploaded document for user {}: {}", userId, documentType);
            
            return ResponseEntity.ok(fileResult);
            
        } catch (Exception e) {
            logger.error("Error processing single document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "classification", "Error",
                        "uploaded", false,
                        "error", e.getMessage()
                    ));
            
        } finally {
            // Clean up temporary file
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.delete(tempFile.toPath());
                    logger.debug("Cleaned up temporary file: {}", tempFile.getPath());
                } catch (IOException e) {
                    logger.warn("Failed to delete temporary file: {}", tempFile.getPath());
                }
            }
        }
    }
    
    @GetMapping("/upload-stats/{userId}")
    public ResponseEntity<?> getUploadStats(@PathVariable String userId) {
        try {
            Map<String, Object> stats = uploadService.getUploadStats(userId);
            return ResponseEntity.ok(Map.of(
                    "userId", userId,
                    "documentCounts", stats
            ));
        } catch (Exception e) {
            logger.error("Error getting upload stats for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get upload statistics"));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "Document Classifier API",
                "version", "1.0.0"
        ));
    }
    
    /**
     * Validate if uploaded file is a ZIP file
     */
    private boolean isZipFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();
        
        return (filename != null && filename.toLowerCase().endsWith(".zip")) ||
               (contentType != null && (contentType.equals("application/zip") || 
                                       contentType.equals("application/x-zip-compressed")));
    }
    
    /**
     * Validate if uploaded file is an image file
     */
    private boolean isImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        // Check content type
        if (contentType != null && contentType.startsWith("image/")) {
            return true;
        }
        
        // Check file extension
        if (filename != null) {
            String lowerFilename = filename.toLowerCase();
            return lowerFilename.endsWith(".png") || 
                   lowerFilename.endsWith(".jpg") || 
                   lowerFilename.endsWith(".jpeg") || 
                   lowerFilename.endsWith(".webp") || 
                   lowerFilename.endsWith(".bmp");
        }
        
        return false;
    }
    
    /**
     * Save uploaded file temporarily for processing
     */
    private File saveUploadedFileTemporarily(MultipartFile file) throws IOException {
        // Create temporary file
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        File tempFile = File.createTempFile("upload_", extension);
        file.transferTo(tempFile);
        
        return tempFile;
    }
}
