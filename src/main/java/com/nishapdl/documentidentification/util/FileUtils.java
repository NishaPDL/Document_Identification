package com.nishapdl.documentidentification.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Utility class for file operations including ZIP extraction and image file validation.
 */
public final class FileUtils {

    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Validates if the uploaded file is a ZIP file.
     *
     * @param file the uploaded file
     * @return true if the file is a ZIP file, false otherwise
     */
    public static boolean isZipFile(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return false;
        }
        return file.getOriginalFilename().toLowerCase().endsWith(Constants.ZIP_FILE_EXTENSION);
    }

    /**
     * Validates if a file has a supported image extension.
     *
     * @param filename the filename to validate
     * @param supportedExtensions set of supported image extensions
     * @return true if the file has a supported image extension, false otherwise
     */
    public static boolean isImageFile(String filename, Set<String> supportedExtensions) {
        if (filename == null || supportedExtensions == null) {
            return false;
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        return supportedExtensions.contains(extension);
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename the filename
     * @return the file extension including the dot (e.g., ".jpg")
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex);
    }

    /**
     * Extracts image files from a ZIP archive to a temporary directory.
     *
     * @param zipFile the ZIP file to extract
     * @param extractToDir the directory to extract files to
     * @param supportedExtensions set of supported image extensions
     * @return list of paths to extracted image files
     * @throws IOException if extraction fails
     */
    public static List<Path> extractImagesFromZip(MultipartFile zipFile, Path extractToDir, 
                                                  Set<String> supportedExtensions) throws IOException {
        List<Path> extractedImagePaths = new ArrayList<>();
        
        try (InputStream inputStream = zipFile.getInputStream();
             ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(inputStream)) {
            
            ZipArchiveEntry entry;
            while ((entry = zipInputStream.getNextZipEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                
                String entryName = entry.getName();
                if (isImageFile(entryName, supportedExtensions)) {
                    Path extractedFilePath = extractFileFromZip(zipInputStream, extractToDir, entryName);
                    extractedImagePaths.add(extractedFilePath);
                    logger.debug("Extracted image file: {}", extractedFilePath);
                }
            }
        }
        
        logger.info("Extracted {} image files from ZIP archive", extractedImagePaths.size());
        return extractedImagePaths;
    }

    /**
     * Extracts a single file from ZIP input stream to the specified directory.
     *
     * @param zipInputStream the ZIP input stream
     * @param extractToDir the directory to extract the file to
     * @param entryName the name of the entry to extract
     * @return path to the extracted file
     * @throws IOException if extraction fails
     */
    private static Path extractFileFromZip(ZipArchiveInputStream zipInputStream, 
                                          Path extractToDir, String entryName) throws IOException {
        // Sanitize the entry name to prevent directory traversal attacks
        String sanitizedName = Paths.get(entryName).getFileName().toString();
        Path extractedFilePath = extractToDir.resolve(sanitizedName);
        
        // Ensure the parent directory exists
        Files.createDirectories(extractedFilePath.getParent());
        
        try (OutputStream outputStream = Files.newOutputStream(extractedFilePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        
        return extractedFilePath;
    }

    /**
     * Creates a temporary directory for file extraction.
     *
     * @param prefix the prefix for the temporary directory name
     * @return path to the created temporary directory
     * @throws IOException if directory creation fails
     */
    public static Path createTempDirectory(String prefix) throws IOException {
        Path tempDir = Files.createTempDirectory(prefix);
        logger.debug("Created temporary directory: {}", tempDir);
        return tempDir;
    }

    /**
     * Recursively deletes a directory and all its contents.
     *
     * @param directory the directory to delete
     */
    public static void deleteDirectoryRecursively(Path directory) {
        try {
            if (Files.exists(directory)) {
                Files.walk(directory)
                     .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                     .forEach(path -> {
                         try {
                             Files.delete(path);
                         } catch (IOException e) {
                             logger.warn("Failed to delete file: {}", path, e);
                         }
                     });
                logger.debug("Deleted temporary directory: {}", directory);
            }
        } catch (IOException e) {
            logger.warn("Failed to delete directory: {}", directory, e);
        }
    }
}
