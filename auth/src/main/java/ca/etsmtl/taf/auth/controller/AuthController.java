package ca.etsmtl.taf.auth.controller;

import ca.etsmtl.taf.auth.cache.TokenCache;
import ca.etsmtl.taf.auth.jwt.JwtUtil;
import ca.etsmtl.taf.auth.payload.request.LoginRequest;
import ca.etsmtl.taf.auth.payload.request.RefreshTokenRequest;
import ca.etsmtl.taf.auth.payload.request.SignupRequest;
import ca.etsmtl.taf.auth.payload.request.ValidateTokenRequest;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import ca.etsmtl.taf.auth.payload.response.MessageResponse;
import ca.etsmtl.taf.auth.services.AuthStrategy;
import ca.etsmtl.taf.auth.services.JwtService;
import ca.etsmtl.taf.auth.services.UserOldService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
public class AuthController {

    @GetMapping("/")
    public String greeting() {
        return "Hello from Auth Microservice!";
    }

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserOldService userService;

    @Autowired
    private TokenCache tokenCache;

    @Autowired
    private JwtUtil jwtUtil;

    private final AuthStrategy authStrategy;

    @Autowired
    public AuthController(AuthStrategy authStrategy) {
        this.authStrategy = authStrategy;
    }


    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> signin(@RequestBody @Valid LoginRequest authenticationRequest) throws Exception {
        log.info("User '{}' attempting sign-in…", authenticationRequest.getUsername());

        ResponseEntity<JwtResponse> resp = authStrategy.signin(authenticationRequest);
        JwtResponse body = resp.getBody();

        if (body != null && body.getToken() != null) {
            // Cache the tokens for auto-refresh
            tokenCache.update(authenticationRequest.getUsername(), body, jwtUtil);
            log.info("🟢 Token cached for '{}'.", authenticationRequest.getUsername());
        }

        log.info("User '{}' signed in successfully.", authenticationRequest.getUsername());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestBody @Valid ValidateTokenRequest validateTokenRequest) throws Exception {
        boolean valid = Boolean.TRUE.equals(authStrategy.validateToken(validateTokenRequest).getBody());
        log.info("Token validation result: {}", valid);
        return ResponseEntity.ok(valid);
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) throws Exception {
        ResponseEntity<?> resp = authStrategy.refreshToken(refreshTokenRequest);
        log.info("Token refresh processed (status={})", resp.getStatusCode());

        // If refresh succeeded, update cache
        if (resp.getBody() instanceof JwtResponse jwtResponse) {
            try {
                String username = jwtUtil.extractUsername(jwtResponse.getToken());
                tokenCache.update(username, jwtResponse, jwtUtil);
                log.info("Token cache updated for '{}'.", username);
            } catch (Exception e) {
                log.warn("Could not update cache after refresh: {}", e.getMessage());
            }
        }
        return resp;
    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest request) {
        log.info("User {} attempting signup…", request.getUsername());
        ResponseEntity<MessageResponse> resp = authStrategy.signup(request);
        if (resp.getStatusCode().is2xxSuccessful()) {
            log.info("Signup succeeded for user {}", request.getUsername());
        } else {
            log.warn("Signup failed for user {} (status={})", request.getUsername(), resp.getStatusCode());
        }
        return resp;
    }


}
