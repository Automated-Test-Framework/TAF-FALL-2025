FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Gradle wrapper and build files first — dependency layer cached
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Download dependencies (cached when build.gradle doesn't change)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build
RUN ./gradlew bootJar --no-daemon

# Runtime stage — JRE only (~400MB smaller)
FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /app/build/libs/auth-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]