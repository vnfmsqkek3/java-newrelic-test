# Multi-stage build for optimized image
FROM openjdk:17-jdk-slim AS builder

WORKDIR /build

# Install Maven and required tools
RUN apt-get update && \
    apt-get install -y maven curl unzip && \
    rm -rf /var/lib/apt/lists/*

# Copy source code
COPY pom.xml .
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Download and extract New Relic agent
RUN curl -fsSLO https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip -q newrelic-java.zip && \
    rm newrelic-java.zip

# Runtime stage
FROM openjdk:17-jre-slim

WORKDIR /app

# Install curl for health checks
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy built application from builder stage
COPY --from=builder /build/target/newrelic-test-1.0.0.jar ./app.jar

# Copy New Relic agent from builder stage
COPY --from=builder /build/newrelic ./newrelic/

# Copy custom New Relic configuration
COPY src/main/resources/newrelic.yml ./newrelic/

# Set ownership
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

EXPOSE 8080

# Environment variables
ENV NEW_RELIC_LICENSE_KEY="bb7a4dc5565cfa8d7771216bcebd99b8FFFFNRAL"
ENV NEW_RELIC_APP_NAME="Java NewRelic Test Application"
ENV JAVA_OPTS="-javaagent:/app/newrelic/newrelic.jar \
               -Dnewrelic.config.app_name=${NEW_RELIC_APP_NAME} \
               -Dnewrelic.config.license_key=${NEW_RELIC_LICENSE_KEY} \
               -Xms256m -Xmx512m \
               -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
