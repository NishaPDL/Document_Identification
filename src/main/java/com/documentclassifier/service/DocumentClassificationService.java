package com.documentclassifier.service;

import com.google.cloud.aiplatform.v1.*;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentClassificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentClassificationService.class);
    
    private final PredictionServiceClient predictionServiceClient;
    private final String projectId;
    private final String location;
    
    @Value("${gemini.model}")
    private String geminiModel;
    
    @Autowired
    public DocumentClassificationService(
            PredictionServiceClient predictionServiceClient,
            String projectId,
            String location) {
        this.predictionServiceClient = predictionServiceClient;
        this.projectId = projectId;
        this.location = location;
    }
    
    /**
     * Classify document type based on extracted text using Gemini model
     */
    public String classifyDocumentType(String extractedText) {
        if (extractedText == null || extractedText.trim().isEmpty()) {
            logger.warn("Empty text provided for classification");
            return "None";
        }
        
        try {
            logger.debug("Classifying document with {} characters of text", extractedText.length());
            
            String prompt = buildClassificationPrompt(extractedText);
            String response = callGeminiModel(prompt);
            
            String classification = parseClassificationResponse(response);
            logger.info("Document classified as: {}", classification);
            
            return classification;
            
        } catch (Exception e) {
            logger.error("Failed to classify document", e);
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * Build the classification prompt for Gemini
     */
    private String buildClassificationPrompt(String extractedText) {
        return String.format("""
                You are an assistant that classifies Indian identity documents based on OCR-extracted text.
                
                Respond with only ONE of the following:
                - Aadhaar
                - PAN
                - Voter ID
                - Driving License
                - None
                
                Here is the extracted text:
                %s
                
                What type of document is this? Respond with only one word.
                """, extractedText);
    }
    
    /**
     * Call Gemini model via Vertex AI
     */
    private String callGeminiModel(String prompt) throws Exception {
        // Build the endpoint name
        String endpoint = EndpointName.ofProjectLocationPublisherModelName(
                projectId, location, "google", geminiModel
        ).toString();
        
        // Create the request payload
        Struct.Builder instanceBuilder = Struct.newBuilder();
        instanceBuilder.putFields("prompt", Value.newBuilder().setStringValue(prompt).build());
        
        Struct.Builder parametersBuilder = Struct.newBuilder();
        parametersBuilder.putFields("temperature", Value.newBuilder().setNumberValue(0.1).build());
        parametersBuilder.putFields("maxOutputTokens", Value.newBuilder().setNumberValue(10).build());
        parametersBuilder.putFields("topP", Value.newBuilder().setNumberValue(0.8).build());
        parametersBuilder.putFields("topK", Value.newBuilder().setNumberValue(40).build());
        
        // Create prediction request
        PredictRequest request = PredictRequest.newBuilder()
                .setEndpoint(endpoint)
                .addInstances(Value.newBuilder().setStructValue(instanceBuilder.build()).build())
                .setParameters(Value.newBuilder().setStructValue(parametersBuilder.build()).build())
                .build();
        
        // Make the prediction
        PredictResponse response = predictionServiceClient.predict(request);
        
        if (response.getPredictionsCount() == 0) {
            throw new RuntimeException("No predictions returned from Gemini model");
        }
        
        // Extract the response text
        Value prediction = response.getPredictions(0);
        if (prediction.hasStructValue()) {
            Struct predictionStruct = prediction.getStructValue();
            if (predictionStruct.containsFields("content")) {
                return predictionStruct.getFieldsMap().get("content").getStringValue();
            }
        }
        
        // Fallback: try to extract text from the prediction directly
        return prediction.getStringValue();
    }
    
    /**
     * Parse and validate the classification response
     */
    private String parseClassificationResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "None";
        }
        
        String cleanResponse = response.trim().toLowerCase();
        
        // Map common variations to standard classifications
        if (cleanResponse.contains("aadhaar") || cleanResponse.contains("aadhar")) {
            return "Aadhaar";
        } else if (cleanResponse.contains("pan")) {
            return "PAN";
        } else if (cleanResponse.contains("voter")) {
            return "Voter ID";
        } else if (cleanResponse.contains("driving") || cleanResponse.contains("license")) {
            return "Driving License";
        } else {
            return "None";
        }
    }
}
