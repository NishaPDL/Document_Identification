package com.nishapdl.documentidentification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests for the Document Identification Application.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "google.cloud.project-id=test-project",
    "google.cloud.location=us-central1",
    "google.cloud.vertex-ai.model=gemini-2.0-flash"
})
class DocumentIdentificationApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
        // with all the required beans and configurations
    }
}
