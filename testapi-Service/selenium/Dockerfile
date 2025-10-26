FROM maven:3.9.9-eclipse-temurin-17 as builder

RUN apt-get update && apt-get install -y wget gnupg curl unzip \
    && wget -qO google-chrome.deb "https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb" \
    && dpkg -i google-chrome.deb || apt-get -fy install \
    && rm google-chrome.deb \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /
COPY pom.xml .
COPY selenium ./selenium
WORKDIR /selenium
RUN mvn clean install

EXPOSE 8090
ENTRYPOINT ["mvn", "spring-boot:run"]
