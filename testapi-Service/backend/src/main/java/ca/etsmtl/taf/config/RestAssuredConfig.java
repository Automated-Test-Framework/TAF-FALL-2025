package ca.etsmtl.taf.config;

import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import static io.restassured.config.HttpClientConfig.httpClientConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;

@Configuration
public class RestAssuredConfig {

    @PostConstruct
    void setup() {
        RestAssured.filters(
                new RequestLoggingFilter(LogDetail.METHOD),
                new RequestLoggingFilter(LogDetail.URI),
                new RequestLoggingFilter(LogDetail.BODY),
                new ResponseLoggingFilter(LogDetail.STATUS),
                new ResponseLoggingFilter(LogDetail.BODY)
        );
        RestAssured.config = newConfig().httpClient(httpClientConfig()
                .setParam("http.connection.timeout", 10000)
                .setParam("http.socket.timeout", 10000)
                .setParam("http.connection-manager.timeout", 10000L));
    }
}
