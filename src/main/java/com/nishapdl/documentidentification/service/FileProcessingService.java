package com.nishapdl.documentidentification.service;

import com.nishapdl.documentidentification.config.ApplicationProperties;
import com.nishapdl.documentidentification.exception.DocumentProcessingException;
import com.nishapdl.documentidentification.util.Constants;
import com.nishapdl.documentidentification.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for handling file processing operations including ZIP extraction and image validation.
 */
@Service
public class FileProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(FileProcessingService.class);

    private final ApplicationProperties applicationProperties;
    private final Set<String> supportedImageExtensions;

    @Autowired
    public FileProcessingService(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.supportedImageExtensions = new HashSet<>(applicationProperties.getSupportedImageExtensions());
    }

    /**
     * Validates that the uploaded file is a ZIP file.
     *
     * @param file the uploaded file to validate
     * @throws IllegalArgumentException if the file is not a ZIP file
     */
    public void validateZipFile(MultipartFile file) {
        if (!FileUtils.isZipFile(file)) {
            throw new IllegalArgumentException(Constants.ERROR_INVALID_FILE_TYPE);
        }
        logger.info("Validated ZIP file: {}", file.getOriginalFilename());
    }

    /**
     * Extracts image files from a ZIP archive.
     *
     * @param zipFile the ZIP file to extract images from
     * @return list of paths to extracted image files
     * @throws DocumentProcessingException if extraction fails or no images are found
     */
    public List<Path> extractImagesFromZip(MultipartFile zipFile) {
        try {
            // Create temporary directory for extraction
            Path tempDir = FileUtils.createTempDirectory("document-extraction-");
            
            // Extract image files from ZIP
            List<Path> extractedImages = FileUtils.extractImagesFromZip(
                zipFile, tempDir, supportedImageExtensions);
            
            if (extractedImages.isEmpty()) {
                // Clean up empty temp directory
                FileUtils.deleteDirectoryRecursively(tempDir);
                throw new IllegalArgumentException(Constants.ERROR_NO_IMAGES_FOUND);
            }
            
            logger.info("Successfully extracted {} image files from ZIP: {}", 
                       extractedImages.size(), zipFile.getOriginalFilename());
            
            return extractedImages;
            
        } catch (IOException e) {
            String errorMessage = String.format(Constants.ERROR_FILE_PROCESSING_FAILED, e.getMessage());
            logger.error("Failed to extract images from ZIP file: {}", zipFile.getOriginalFilename(), e);
            throw new DocumentProcessingException(errorMessage, e);
        }
    }

    /**
     * Cleans up temporary files and directories.
     *
     * @param imagePaths list of image file paths to clean up
     */
    public void cleanupTempFiles(List<Path> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return;
        }
        
        // Get the parent directory (temp directory) from the first image path
        Path tempDir = imagePaths.get(0).getParent();
        if (tempDir != null) {
            FileUtils.deleteDirectoryRecursively(tempDir);
            logger.debug("Cleaned up temporary directory: {}", tempDir);
        }
    }

    /**
     * Gets the set of supported image extensions.
     *
     * @return set of supported image extensions
     */
    public Set<String> getSupportedImageExtensions() {
        return new HashSet<>(supportedImageExtensions);
    }
}
