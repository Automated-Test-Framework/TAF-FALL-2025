package org.requests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"org.requests", "org.config"})
public class TestManager {
    public static void main(String[] args) {
        SpringApplication.run(TestManager.class, args);
    }
}
