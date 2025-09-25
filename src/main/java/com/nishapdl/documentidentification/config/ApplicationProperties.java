package com.nishapdl.documentidentification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Configuration properties for the Document Identification application.
 * Maps application configuration from application.yml.
 */
@Component
@ConfigurationProperties(prefix = "app")
public class ApplicationProperties {

    private List<String> supportedImageExtensions;
    private List<String> documentTypes;

    public List<String> getSupportedImageExtensions() {
        return supportedImageExtensions;
    }

    public void setSupportedImageExtensions(List<String> supportedImageExtensions) {
        this.supportedImageExtensions = supportedImageExtensions;
    }

    public List<String> getDocumentTypes() {
        return documentTypes;
    }

    public void setDocumentTypes(List<String> documentTypes) {
        this.documentTypes = documentTypes;
    }
}
