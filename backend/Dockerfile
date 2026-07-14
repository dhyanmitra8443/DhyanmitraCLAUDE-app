# Ref: SRS 2.13, 19.9 - Docker image for the Spring Boot backend, built via
# Docker Compose as part of the deployment strategy.

# ---- Build stage --------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage -------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S lms && adduser -S lms -G lms
COPY --from=build /app/target/lms-backend-*.jar app.jar
# Owned by lms before the volume mount attaches, so Docker's first-mount
# content copy into the (empty) named volume preserves this ownership -
# otherwise /app/uploads ends up root-owned and LocalFileStorageService
# gets AccessDeniedException writing to it as the non-root lms user.
RUN mkdir -p /app/uploads && chown -R lms:lms /app/uploads
USER lms
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
