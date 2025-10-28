FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

# Convert line endings and make executable, then build
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar

# Run the app
CMD ["java", "-jar", "build/libs/auth-0.0.1-SNAPSHOT.jar"]