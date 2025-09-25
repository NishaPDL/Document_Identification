# Multi-stage Docker build for Enhanced Document Identification API v2.0

# Stage 1: Build stage
FROM openjdk:17-jdk-slim AS builder

# Set working directory
WORKDIR /app

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Copy Maven wrapper and pom.xml first (for better caching)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make Maven wrapper executable
RUN chmod +x ./mvnw

# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application with tests
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM openjdk:17-jre-slim

# Set metadata
LABEL maintainer="pythonai@paisalo.in"
LABEL version="2.0.0"
LABEL description="Enhanced Document Identification API with AI/ML capabilities"

# Install runtime dependencies
RUN apt-get update && apt-get install -y \
    curl \
    dumb-init \
    && rm -rf /var/lib/apt/lists/*

# Create application user and group
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/document-identification-api-2.0.0.jar app.jar

# Create directories and set permissions
RUN mkdir -p /app/logs /app/temp /app/config && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+OptimizeStringConcat \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.backgroundpreinitializer.ignore=true"

# Application configuration
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# Alternative: Direct Java execution (uncomment if preferred)
# CMD ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
