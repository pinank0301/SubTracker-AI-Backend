# ==============================================================================
# Notification Service — Multi-stage Production Dockerfile
# Java 21, Spring Boot, Maven
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build Stage
# ------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy Maven POM and pre-fetch dependencies to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and build production executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Minimal Runtime Stage
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create dedicated non-root user and group for container security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy repackaged Spring Boot JAR from builder
COPY --from=builder /build/target/*.jar app.jar
RUN chown -R spring:spring /app

USER spring:spring

# Conservative JVM memory parameters suitable for multi-service small EC2 instance
ENV JAVA_OPTS="-Xms256m -Xmx384m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Notification Service port
EXPOSE 8083

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
