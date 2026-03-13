package org.config;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeoutConfig {

    @Value("${timeout.connection:10000}")
    private int connectionTimeout;

    @Value("${timeout.socket:10000}")
    private int socketTimeout;

    @Value("${timeout.connectionManager:10000}")
    private int connectionManagerTimeout;

    @PostConstruct
    public void configureRestAssuredTimeout() {
        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", connectionTimeout)
                        .setParam("http.socket.timeout", socketTimeout)
                        .setParam("http.connection-manager.timeout", connectionManagerTimeout));

        System.out.println("✅ RestAssured timeout configured: connection=" + connectionTimeout
                + "ms, socket=" + socketTimeout + "ms, connectionManager=" + connectionManagerTimeout + "ms");
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public int getConnectionManagerTimeout() {
        return connectionManagerTimeout;
    }
}
