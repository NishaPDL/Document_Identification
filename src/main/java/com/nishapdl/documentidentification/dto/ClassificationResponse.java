package com.nishapdl.documentidentification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Response DTO for document classification results.
 * Contains a map of filename to document type classification.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClassificationResponse {

    private Map<String, String> results;
    private String message;
    private boolean success;

    public ClassificationResponse() {
    }

    public ClassificationResponse(Map<String, String> results) {
        this.results = results;
        this.success = true;
    }

    public ClassificationResponse(Map<String, String> results, String message) {
        this.results = results;
        this.message = message;
        this.success = true;
    }

    public static ClassificationResponse success(Map<String, String> results) {
        return new ClassificationResponse(results);
    }

    public static ClassificationResponse success(Map<String, String> results, String message) {
        return new ClassificationResponse(results, message);
    }

    public static ClassificationResponse error(String message) {
        ClassificationResponse response = new ClassificationResponse();
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }

    // Getters and Setters
    public Map<String, String> getResults() {
        return results;
    }

    public void setResults(Map<String, String> results) {
        this.results = results;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
