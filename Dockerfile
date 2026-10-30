# ==============================================================================
# Dockerfile for Experiment 9: CI/CD Pipeline Java Application
# Multi-stage build for minimal image size and enhanced security
# ==============================================================================

# Stage 1: Build stage with OpenJDK 17 and Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace/app

# Copy pom.xml and pre-fetch dependencies (optimizes Docker layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime JRE image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root system user for security compliance
RUN useradd -u 1001 -m cicduser
USER cicduser

# Copy built executable jar from builder stage
COPY --from=builder /workspace/app/target/experiment-9-cicd-pipeline-1.0.0.jar app.jar

# Expose Spring Boot application port
EXPOSE 8089

# Environment variables
ENV SERVER_PORT=8089
ENV SPRING_PROFILES_ACTIVE=prod

# Healthcheck to verify container health during deployments
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8089/api/pipeline/overview || exit 1

# Execute Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
