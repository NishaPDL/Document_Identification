# Document Identification API - Java Spring Boot

A Java Spring Boot application that classifies Indian identity documents using Google Cloud Vision API for OCR and Vertex AI Gemini model for document type classification.

## Overview

This application processes ZIP files containing document images and classifies them into the following categories:
- **Aadhaar** - Indian national identity card
- **PAN** - Permanent Account Number card
- **Voter ID** - Election Commission identity card
- **Driving License** - Motor vehicle driving license
- **None** - Unrecognized document type

## Features

- 🔍 **OCR Processing**: Uses Google Cloud Vision API for text extraction from images
- 🤖 **AI Classification**: Leverages Vertex AI Gemini model for document type identification
- 📁 **Batch Processing**: Handles multiple images in ZIP archives
- 🛡️ **Error Handling**: Comprehensive error handling and validation
- 📊 **RESTful API**: Clean REST endpoints with JSON responses
- 🔧 **Configurable**: Environment-based configuration
- 📝 **Logging**: Structured logging with SLF4J

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Maven** for dependency management
- **Google Cloud Vision API** for OCR
- **Google Cloud Vertex AI** for document classification
- **Apache Commons Compress** for ZIP file handling

## Prerequisites

1. **Java 17** or higher
2. **Maven 3.6+**
3. **Google Cloud Project** with the following APIs enabled:
   - Cloud Vision API
   - Vertex AI API
4. **Google Cloud Service Account** with appropriate permissions
5. **Service Account Key** (JSON file) for authentication

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/NishaPDL/Document_Identification.git
cd Document_Identification
```

### 2. Google Cloud Setup

1. Create a Google Cloud Project
2. Enable the required APIs:
   ```bash
   gcloud services enable vision.googleapis.com
   gcloud services enable aiplatform.googleapis.com
   ```
3. Create a service account and download the JSON key file
4. Set the environment variable:
   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS="path/to/your/service-account-key.json"
   ```

### 3. Configuration

Update the `src/main/resources/application.yml` file with your Google Cloud settings:

```yaml
google:
  cloud:
    project-id: your-project-id
    location: us-central1
    vertex-ai:
      model: gemini-2.0-flash
```

Or set environment variables:
```bash
export GOOGLE_CLOUD_PROJECT=your-project-id
export GOOGLE_CLOUD_LOCATION=us-central1
export GEMINI_MODEL=gemini-2.0-flash
```

### 4. Build and Run

```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### 1. Classify Documents

**POST** `/api/v1/classify-documents`

Upload a ZIP file containing document images for classification.

**Request:**
- Content-Type: `multipart/form-data`
- Parameter: `file` (ZIP file)

**Response:**
```json
{
  "image1.jpg": "PAN",
  "image2.png": "Aadhaar",
  "image3.webp": "Voter ID"
}
```

**Example using cURL:**
```bash
curl -X POST \
  http://localhost:8080/api/v1/classify-documents \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@documents.zip'
```

### 2. Health Check

**GET** `/api/v1/health`

Check the application health status.

**Response:**
```json
{
  "status": "UP",
  "service": "Document Identification API",
  "version": "1.0.0"
}
```

### 3. Supported Document Types

**GET** `/api/v1/document-types`

Get information about supported document types and image formats.

**Response:**
```json
{
  "supportedTypes": ["Aadhaar", "PAN", "Voter ID", "Driving License", "None"],
  "supportedImageFormats": [".png", ".jpg", ".jpeg", ".webp", ".bmp"]
}
```

## Supported Image Formats

- PNG (.png)
- JPEG (.jpg, .jpeg)
- WebP (.webp)
- BMP (.bmp)

## Error Handling

The API returns appropriate HTTP status codes and error messages:

- **400 Bad Request**: Invalid file type or no images found
- **413 Payload Too Large**: File size exceeds limit (50MB)
- **500 Internal Server Error**: Processing errors

Example error response:
```json
{
  "error": "Only ZIP files are allowed."
}
```

## Configuration Options

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | 8080 | Server port |
| `spring.servlet.multipart.max-file-size` | 50MB | Maximum file size |
| `google.cloud.project-id` | pdlgcpcloud | Google Cloud project ID |
| `google.cloud.location` | us-central1 | Google Cloud region |
| `google.cloud.vertex-ai.model` | gemini-2.0-flash | Gemini model version |

### Environment Variables

- `GOOGLE_APPLICATION_CREDENTIALS`: Path to service account key file
- `GOOGLE_CLOUD_PROJECT`: Google Cloud project ID
- `GOOGLE_CLOUD_LOCATION`: Google Cloud region
- `GEMINI_MODEL`: Vertex AI model name

## Docker Support

### Build Docker Image

```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t document-identification-api .
```

### Run with Docker

```bash
docker run -p 8080:8080 \
  -e GOOGLE_APPLICATION_CREDENTIALS=/app/service-account-key.json \
  -e GOOGLE_CLOUD_PROJECT=your-project-id \
  -v /path/to/service-account-key.json:/app/service-account-key.json \
  document-identification-api
```

## Development

### Running Tests

```bash
mvn test
```

### Code Style

The project follows standard Java coding conventions with:
- Proper JavaDoc documentation
- Comprehensive error handling
- Structured logging
- Clean architecture with separation of concerns

### Project Structure

```
src/
├── main/
│   ├── java/com/nishapdl/documentidentification/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data transfer objects
│   │   ├── exception/      # Custom exceptions
│   │   ├── service/        # Business logic services
│   │   └── util/           # Utility classes
│   └── resources/
│       ├── application.yml  # Application configuration
│       └── document_mapping.json  # Sample mappings
└── test/                   # Test classes
```

## Migration from Python

This Java application is a direct conversion of the original Python FastAPI application with the following improvements:

- **Type Safety**: Strong typing with Java
- **Performance**: Better performance for concurrent requests
- **Enterprise Features**: Built-in Spring Boot features like health checks, metrics, and configuration management
- **Scalability**: Better suited for enterprise deployments
- **Maintainability**: Clean architecture with proper separation of concerns

## Troubleshooting

### Common Issues

1. **Authentication Error**: Ensure `GOOGLE_APPLICATION_CREDENTIALS` is set correctly
2. **API Not Enabled**: Enable Vision API and Vertex AI API in Google Cloud Console
3. **Permission Denied**: Ensure service account has necessary permissions
4. **File Size Error**: Check file size limits in configuration
5. **Memory Issues**: Increase JVM heap size for large files

### Logging

Enable debug logging for troubleshooting:

```yaml
logging:
  level:
    com.nishapdl.documentidentification: DEBUG
    com.google.cloud: DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
- Create an issue in the GitHub repository
- Check the troubleshooting section
- Review the logs for error details
