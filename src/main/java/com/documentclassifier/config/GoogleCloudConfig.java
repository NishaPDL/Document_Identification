package com.documentclassifier.config;

import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GoogleCloudConfig {

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${google.cloud.location}")
    private String location;

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        return ImageAnnotatorClient.create();
    }

    @Bean
    public PredictionServiceClient predictionServiceClient() throws IOException {
        String endpoint = String.format("%s-aiplatform.googleapis.com:443", location);
        PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                .setEndpoint(endpoint)
                .build();
        return PredictionServiceClient.create(settings);
    }

    @Bean
    public String projectId() {
        return projectId;
    }

    @Bean
    public String location() {
        return location;
    }
}
