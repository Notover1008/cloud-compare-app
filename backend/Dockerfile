# Multi-stage build for production deployment

# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create minimal runtime image with Distroless
FROM gcr.io/distroless/java17-debian12

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Distroless images run as non-root user 'nonroot' (UID 65532) by default
USER nonroot:nonroot

# Expose application port
EXPOSE 8080

# Run the application
# No shell, no package managers, only JRE + app
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]