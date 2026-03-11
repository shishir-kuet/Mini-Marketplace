# Build stage
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Install Node.js for the React frontend build
RUN apt-get update && apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY pom.xml .

# Copy frontend only if it exists (teammate's contribution)
# When frontend/package.json is present the React app is built inside Maven.
# Otherwise use -Pno-frontend to skip the frontend build step.
COPY . .

# Build: skip frontend if package.json is missing, otherwise full build
RUN if [ -f frontend/package.json ]; then \
        mvn clean package -DskipTests; \
    else \
        mvn clean package -DskipTests -Pno-frontend; \
    fi

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar file
COPY --from=build /app/target/*.jar app.jar

# Create a non-root user
RUN addgroup -g 1001 -S appgroup && \
    adduser -S appuser -u 1001 -G appgroup

# Change ownership of the app directory
RUN chown -R appuser:appgroup /app
USER appuser

# Expose port (Render overrides this with PORT env var)
EXPOSE 8083

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8083}/api/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]