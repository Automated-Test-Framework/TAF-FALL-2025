package ca.etsmtl.taf.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "TAF - Test Automation Framework API",
        version = "1.0",
        description = "API pour le framework d'automatisation de tests (TAF). "
            + "**Authentification JWT** : POST /api/auth/signin -> token JWT, puis Authorize -> Bearer <token>. "
            + "**Connexion Google OAuth2** : Ouvrez /oauth2/authorization/google dans le navigateur -> "
            + "connexion Google -> redirection avec token JWT."
    ),
    servers = {
        @Server(url = "http://localhost:8084", description = "Local Docker (team2)"),
        @Server(url = "http://localhost:8080", description = "Via Gateway"),
        @Server(url = "/", description = "Relative (current host)")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Entrez le token JWT obtenu via /api/auth/signin ou via Google OAuth2"
)
public class OpenApiConfig {
}
