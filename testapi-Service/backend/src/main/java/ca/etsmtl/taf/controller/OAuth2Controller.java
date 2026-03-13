package ca.etsmtl.taf.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * OAuth2 related endpoints.
 * The actual Google login flow is handled by Spring Security at /oauth2/authorization/google.
 * This controller provides supplementary API endpoints.
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/oauth2")
@Tag(name = "OAuth2 Authentication", description = "Google OAuth2 login endpoints")
public class OAuth2Controller {

    @GetMapping("/login-url")
    @Operation(
        summary = "Get Google OAuth2 login URL",
        description = "Returns the URL to initiate Google OAuth2 login. "
            + "Redirect the user's browser to this URL to start the Google login flow."
    )
    public ResponseEntity<?> getGoogleLoginUrl() {
        return ResponseEntity.ok(Map.of(
            "provider", "google",
            "loginUrl", "/oauth2/authorization/google",
            "description", "Redirect user's browser to this URL to initiate Google login"
        ));
    }
}
