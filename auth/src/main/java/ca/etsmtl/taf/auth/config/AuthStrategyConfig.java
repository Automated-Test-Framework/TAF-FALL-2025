package ca.etsmtl.taf.auth.config;

import ca.etsmtl.taf.auth.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AuthStrategyConfig {

    private final JwtAuthStrategy jwtAuthStrategy;
    private final OAuthAuthStrategy oAuthAuthStrategy;
    private final BasicAuthStrategy basicAuthStrategy;

    @Value("${feature.auth.mode:jwt}")
    private String authMode;

    @Bean
    public AuthStrategy authStrategy() {
        return switch (authMode.toLowerCase()) {
            case "basic" -> basicAuthStrategy;
            case "jwt" -> jwtAuthStrategy;
            case "oauth" -> oAuthAuthStrategy;
            default -> throw new IllegalStateException("Invalid authentication mode: " + authMode);
        };
    }
}
