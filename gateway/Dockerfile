FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

# Fix line endings and make gradlew executable
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Build the JAR inside Docker
RUN ./gradlew bootJar

# Run the app
CMD ["java", "-jar", "build/libs/gateway-0.0.1-SNAPSHOT.jar"]