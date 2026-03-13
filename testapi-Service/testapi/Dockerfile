FROM maven:3.9.5-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy POM first — dependency layer cached when pom.xml doesn't change
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code (only invalidates when src/ changes)
COPY src ./src

# Build
RUN mvn clean package -DskipTests -B

# Runtime stage — slim JRE instead of full Maven+JDK (~800MB → ~200MB)
FROM eclipse-temurin:17-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]