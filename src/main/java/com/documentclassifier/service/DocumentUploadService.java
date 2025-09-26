package com.documentclassifier.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@Service
public class DocumentUploadService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentUploadService.class);
    
    @Value("${document.upload.base-path:/uploads}")
    private String baseUploadPath;
    
    // Document type to folder mapping
    private static final Map<String, String> DOCUMENT_TYPE_PATHS = Map.of(
        "Aadhaar", "aadhaar",
        "PAN", "pan",
        "Voter ID", "voter-id",
        "Driving License", "driving-license"
    );
    
    /**
     * Upload classified documents to their respective folders
     */
    public Map<String, Object> uploadClassifiedDocuments(
            Map<String, String> classificationResults, 
            Map<String, File> imageFiles, 
            String userId) {
        
        Map<String, Object> uploadResults = new HashMap<>();
        
        for (Map.Entry<String, String> entry : classificationResults.entrySet()) {
            String filename = entry.getKey();
            String documentType = entry.getValue();
            File imageFile = imageFiles.get(filename);
            
            try {
                if (imageFile != null && !documentType.startsWith("Error") && !documentType.equals("None")) {
                    String uploadPath = uploadDocument(imageFile, documentType, userId, filename);
                    uploadResults.put(filename, Map.of(
                        "classification", documentType,
                        "uploaded", true,
                        "path", uploadPath
                    ));
                    logger.info("Successfully uploaded {} as {} to {}", filename, documentType, uploadPath);
                } else {
                    uploadResults.put(filename, Map.of(
                        "classification", documentType,
                        "uploaded", false,
                        "reason", documentType.startsWith("Error") ? "Classification error" : 
                                 documentType.equals("None") ? "Document type not recognized" : "File not found"
                    ));
                }
            } catch (Exception e) {
                logger.error("Failed to upload {}: {}", filename, e.getMessage());
                uploadResults.put(filename, Map.of(
                    "classification", documentType,
                    "uploaded", false,
                    "error", e.getMessage()
                ));
            }
        }
        
        return uploadResults;
    }
    
    /**
     * Upload a single document to the appropriate folder
     */
    private String uploadDocument(File sourceFile, String documentType, String userId, String originalFilename) 
            throws IOException {
        
        // Get the folder path for this document type
        String folderName = DOCUMENT_TYPE_PATHS.getOrDefault(documentType, "others");
        
        // Create the full upload path: base/userId/documentType/filename
        Path uploadDir = Paths.get(baseUploadPath, userId, folderName);
        
        // Create directories if they don't exist
        Files.createDirectories(uploadDir);
        
        // Generate unique filename to avoid conflicts
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = String.format("%s_%d%s", 
            removeFileExtension(originalFilename), 
            System.currentTimeMillis(), 
            fileExtension);
        
        Path targetPath = uploadDir.resolve(uniqueFilename);
        
        // Copy the file to the target location
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        return targetPath.toString();
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }
    
    /**
     * Remove file extension from filename
     */
    private String removeFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(0, lastDotIndex) : filename;
    }
    
    /**
     * Get upload statistics
     */
    public Map<String, Object> getUploadStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            Path userDir = Paths.get(baseUploadPath, userId);
            if (Files.exists(userDir)) {
                for (String docType : DOCUMENT_TYPE_PATHS.values()) {
                    Path docTypeDir = userDir.resolve(docType);
                    if (Files.exists(docTypeDir)) {
                        long fileCount = Files.list(docTypeDir).count();
                        stats.put(docType, fileCount);
                    } else {
                        stats.put(docType, 0);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error getting upload stats for user {}: {}", userId, e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Clean up temporary files after upload
     */
    public void cleanupTempFiles(Map<String, File> imageFiles) {
        for (File file : imageFiles.values()) {
            try {
                if (file.exists()) {
                    Files.delete(file.toPath());
                }
            } catch (IOException e) {
                logger.warn("Failed to delete temporary file {}: {}", file.getPath(), e.getMessage());
            }
        }
    }
}
