package com.nishapdl.documentidentification.service;

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.nishapdl.documentidentification.config.GoogleCloudConfig;
import com.nishapdl.documentidentification.exception.DocumentProcessingException;
import com.nishapdl.documentidentification.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for classifying document types using Vertex AI Gemini model.
 */
@Service
public class DocumentClassificationService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentClassificationService.class);

    private final PredictionServiceClient predictionServiceClient;
    private final GoogleCloudConfig googleCloudConfig;
    private final OCRService ocrService;
    private final FileProcessingService fileProcessingService;

    @Autowired
    public DocumentClassificationService(PredictionServiceClient predictionServiceClient,
                                       GoogleCloudConfig googleCloudConfig,
                                       OCRService ocrService,
                                       FileProcessingService fileProcessingService) {
        this.predictionServiceClient = predictionServiceClient;
        this.googleCloudConfig = googleCloudConfig;
        this.ocrService = ocrService;
        this.fileProcessingService = fileProcessingService;
    }

    /**
     * Classifies a document type based on extracted text using Vertex AI Gemini model.
     *
     * @param extractedText the text extracted from the document image
     * @return the classified document type
     * @throws DocumentProcessingException if classification fails
     */
    public String classifyDocumentType(String extractedText) {
        try {
            logger.debug("Starting document classification for text length: {}", extractedText.length());
            
            if (extractedText == null || extractedText.trim().isEmpty()) {
                logger.warn("Empty or null text provided for classification");
                return Constants.DOCUMENT_TYPE_NONE;
            }
            
            // Format the prompt with the extracted text
            String prompt = String.format(Constants.GEMINI_CLASSIFICATION_PROMPT, extractedText);
            
            // Build the prediction request
            String endpoint = String.format("projects/%s/locations/%s/publishers/google/models/%s",
                                          googleCloudConfig.getProjectId(),
                                          googleCloudConfig.getLocation(),
                                          googleCloudConfig.getGeminiModel());
            
            // Create the request parameters
            Struct.Builder instanceBuilder = Struct.newBuilder();
            instanceBuilder.putFields("prompt", Value.newBuilder().setStringValue(prompt).build());
            
            Struct.Builder parametersBuilder = Struct.newBuilder();
            parametersBuilder.putFields("temperature", Value.newBuilder().setNumberValue(0.1).build());
            parametersBuilder.putFields("maxOutputTokens", Value.newBuilder().setNumberValue(10).build());
            parametersBuilder.putFields("topP", Value.newBuilder().setNumberValue(0.8).build());
            parametersBuilder.putFields("topK", Value.newBuilder().setNumberValue(40).build());
            
            PredictRequest request = PredictRequest.newBuilder()
                    .setEndpoint(endpoint)
                    .addInstances(Value.newBuilder().setStructValue(instanceBuilder.build()).build())
                    .setParameters(Value.newBuilder().setStructValue(parametersBuilder.build()).build())
                    .build();
            
            // Make the prediction
            PredictResponse response = predictionServiceClient.predict(request);
            
            if (response.getPredictionsCount() == 0) {
                throw new DocumentProcessingException("No predictions received from Gemini model");
            }
            
            // Extract the classification result
            Value prediction = response.getPredictions(0);
            String classificationResult = extractClassificationFromResponse(prediction);
            
            logger.info("Document classified as: {}", classificationResult);
            return classificationResult;
            
        } catch (Exception e) {
            String errorMessage = String.format(Constants.ERROR_CLASSIFICATION_FAILED, e.getMessage());
            logger.error("Document classification failed for text: {}", 
                        extractedText.length() > 100 ? extractedText.substring(0, 100) + "..." : extractedText, e);
            throw new DocumentProcessingException(errorMessage, e);
        }
    }

    /**
     * Processes multiple images: extracts text and classifies document types.
     *
     * @param imagePaths list of paths to image files
     * @return map of filename to document type classification
     */
    public Map<String, String> classifyDocuments(List<Path> imagePaths) {
        Map<String, String> results = new HashMap<>();
        
        try {
            logger.info("Starting document classification for {} images", imagePaths.size());
            
            for (Path imagePath : imagePaths) {
                String filename = imagePath.getFileName().toString();
                
                try {
                    // Extract text from image
                    String extractedText = ocrService.extractTextFromImage(imagePath);
                    
                    // Classify document type
                    String documentType = classifyDocumentType(extractedText);
                    
                    results.put(filename, documentType);
                    logger.debug("Processed image: {} -> {}", filename, documentType);
                    
                } catch (Exception e) {
                    String errorMessage = String.format("Error: %s", e.getMessage());
                    results.put(filename, errorMessage);
                    logger.warn("Failed to process image: {}, error: {}", filename, e.getMessage());
                }
            }
            
            logger.info("Completed document classification for {} images", imagePaths.size());
            return results;
            
        } finally {
            // Clean up temporary files
            fileProcessingService.cleanupTempFiles(imagePaths);
        }
    }

    /**
     * Extracts the classification result from the Gemini model response.
     *
     * @param prediction the prediction value from the response
     * @return the extracted classification result
     */
    private String extractClassificationFromResponse(Value prediction) {
        try {
            // The response structure may vary, so we need to handle different formats
            if (prediction.hasStringValue()) {
                return prediction.getStringValue().trim();
            } else if (prediction.hasStructValue()) {
                Struct struct = prediction.getStructValue();
                // Try to find the content in common response fields
                if (struct.containsFields("content")) {
                    return struct.getFieldsMap().get("content").getStringValue().trim();
                } else if (struct.containsFields("text")) {
                    return struct.getFieldsMap().get("text").getStringValue().trim();
                } else if (struct.containsFields("candidates")) {
                    // Handle candidates array structure
                    Value candidates = struct.getFieldsMap().get("candidates");
                    if (candidates.hasListValue() && candidates.getListValue().getValuesCount() > 0) {
                        Value firstCandidate = candidates.getListValue().getValues(0);
                        if (firstCandidate.hasStructValue()) {
                            Struct candidateStruct = firstCandidate.getStructValue();
                            if (candidateStruct.containsFields("content")) {
                                return candidateStruct.getFieldsMap().get("content").getStringValue().trim();
                            }
                        }
                    }
                }
            }
            
            // If we can't extract the classification, return None as fallback
            logger.warn("Could not extract classification from response, returning None");
            return Constants.DOCUMENT_TYPE_NONE;
            
        } catch (Exception e) {
            logger.error("Error extracting classification from response: {}", e.getMessage(), e);
            return Constants.DOCUMENT_TYPE_NONE;
        }
    }
}
