package com.nishapdl.documentidentification.service;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import com.nishapdl.documentidentification.exception.DocumentProcessingException;
import com.nishapdl.documentidentification.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for performing Optical Character Recognition (OCR) using Google Cloud Vision API.
 */
@Service
public class OCRService {

    private static final Logger logger = LoggerFactory.getLogger(OCRService.class);

    private final ImageAnnotatorClient imageAnnotatorClient;

    @Autowired
    public OCRService(ImageAnnotatorClient imageAnnotatorClient) {
        this.imageAnnotatorClient = imageAnnotatorClient;
    }

    /**
     * Extracts text from an image file using Google Cloud Vision API.
     *
     * @param imagePath path to the image file
     * @return extracted text from the image
     * @throws DocumentProcessingException if OCR processing fails
     */
    public String extractTextFromImage(Path imagePath) {
        try {
            logger.debug("Starting OCR processing for image: {}", imagePath);
            
            // Read image file
            byte[] imageData = Files.readAllBytes(imagePath);
            ByteString imgBytes = ByteString.copyFrom(imageData);
            
            // Build the image
            Image image = Image.newBuilder().setContent(imgBytes).build();
            
            // Set the feature type to text detection
            Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
            
            // Build the request
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feature)
                    .setImage(image)
                    .build();
            
            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);
            
            // Perform the request
            BatchAnnotateImagesResponse response = imageAnnotatorClient.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();
            
            if (responses.isEmpty()) {
                throw new DocumentProcessingException("No response received from Vision API");
            }
            
            AnnotateImageResponse imageResponse = responses.get(0);
            
            // Check for errors
            if (imageResponse.hasError()) {
                String errorMessage = String.format(Constants.ERROR_OCR_FAILED, 
                                                   imageResponse.getError().getMessage());
                throw new DocumentProcessingException(errorMessage);
            }
            
            // Extract text
            String extractedText = "";
            if (imageResponse.hasFullTextAnnotation()) {
                extractedText = imageResponse.getFullTextAnnotation().getText().trim();
            }
            
            logger.info("Successfully extracted text from image: {} (text length: {} characters)", 
                       imagePath.getFileName(), extractedText.length());
            logger.debug("Extracted text preview: {}", 
                        extractedText.length() > 100 ? extractedText.substring(0, 100) + "..." : extractedText);
            
            return extractedText;
            
        } catch (IOException e) {
            String errorMessage = String.format(Constants.ERROR_OCR_FAILED, e.getMessage());
            logger.error("Failed to read image file for OCR: {}", imagePath, e);
            throw new DocumentProcessingException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = String.format(Constants.ERROR_OCR_FAILED, e.getMessage());
            logger.error("OCR processing failed for image: {}", imagePath, e);
            throw new DocumentProcessingException(errorMessage, e);
        }
    }

    /**
     * Extracts text from multiple image files.
     *
     * @param imagePaths list of paths to image files
     * @return list of extracted texts corresponding to each image
     */
    public List<String> extractTextFromImages(List<Path> imagePaths) {
        List<String> extractedTexts = new ArrayList<>();
        
        for (Path imagePath : imagePaths) {
            try {
                String extractedText = extractTextFromImage(imagePath);
                extractedTexts.add(extractedText);
            } catch (DocumentProcessingException e) {
                // Log the error but continue processing other images
                logger.warn("Failed to extract text from image: {}, error: {}", imagePath, e.getMessage());
                extractedTexts.add(""); // Add empty string for failed extractions
            }
        }
        
        logger.info("Completed OCR processing for {} images", imagePaths.size());
        return extractedTexts;
    }
}
