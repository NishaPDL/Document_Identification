package com.nishapdl.documentidentification.config;

import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Configuration class for Google Cloud services.
 * Initializes Google Cloud Vision API and Vertex AI clients.
 */
@Configuration
public class GoogleCloudConfig {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCloudConfig.class);

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${google.cloud.location}")
    private String location;

    @Value("${google.cloud.vertex-ai.model}")
    private String geminiModel;

    /**
     * Creates and configures Google Cloud Vision API client.
     * 
     * @return ImageAnnotatorClient for OCR operations
     * @throws IOException if client initialization fails
     */
    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        try {
            ImageAnnotatorClient client = ImageAnnotatorClient.create();
            logger.info("Successfully initialized Google Cloud Vision API client");
            return client;
        } catch (IOException e) {
            logger.error("Failed to initialize Google Cloud Vision API client: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Creates and configures Vertex AI Prediction Service client.
     * 
     * @return PredictionServiceClient for AI model predictions
     * @throws IOException if client initialization fails
     */
    @Bean
    public PredictionServiceClient predictionServiceClient() throws IOException {
        try {
            String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
            PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                    .setEndpoint(endpoint)
                    .build();
            
            PredictionServiceClient client = PredictionServiceClient.create(settings);
            logger.info("Successfully initialized Vertex AI Prediction Service client for project: {}, location: {}", 
                       projectId, location);
            return client;
        } catch (IOException e) {
            logger.error("Failed to initialize Vertex AI Prediction Service client: {}", e.getMessage());
            throw e;
        }
    }

    // Getters for configuration values
    public String getProjectId() {
        return projectId;
    }

    public String getLocation() {
        return location;
    }

    public String getGeminiModel() {
        return geminiModel;
    }
}
