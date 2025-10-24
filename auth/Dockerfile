# Build stage: use Gradle image with JDK21 to build the project
FROM gradle:8-jdk21 AS builder
WORKDIR /home/gradle/project
# copy sources and Gradle wrapper; keep file ownership for Gradle
COPY --chown=gradle:gradle . .
# use BuildKit cache for Gradle dependencies to speed up rebuilds
RUN --mount=type=cache,target=/home/gradle/.gradle gradle clean build -x test --no-daemon --warning-mode all --stacktrace

# Runtime stage: use OpenJDK 21
FROM openjdk:21-jdk
WORKDIR /opt/app
# copy the built jar from the builder stage
COPY --from=builder /home/gradle/project/build/libs/*.jar /app.jar
EXPOSE 8763
ENTRYPOINT ["java","-jar","/app.jar"]