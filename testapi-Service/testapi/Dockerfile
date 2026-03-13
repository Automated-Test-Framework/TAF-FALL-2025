FROM maven:3.9.5-eclipse-temurin-17 as builder

WORKDIR /testapi
COPY . .

RUN mvn clean install -DskipTests -B

EXPOSE 8082
ENTRYPOINT ["mvn", "spring-boot:run" ]