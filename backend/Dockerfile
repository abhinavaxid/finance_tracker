# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM maven:3.8.1-openjdk-8 as builder

WORKDIR /build

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:resolve -DskipTests

# Copy source code
COPY src src/

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:8-jre-slim

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /build/target/expense-tracker-*.jar app.jar

# Create non-root user for security
RUN useradd -m -u 1000 appuser && chown appuser:appuser /app
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# Spring Boot application entry point
ENTRYPOINT ["java", "-jar", "app.jar"]

# Default to dev profile, can be overridden
CMD ["--spring.profiles.active=dev"]

# Metadata
LABEL maintainer="Finance Tracker Team"
LABEL version="1.0.0"
LABEL description="Smart Expense Tracker with Budgeting & Analytics"
