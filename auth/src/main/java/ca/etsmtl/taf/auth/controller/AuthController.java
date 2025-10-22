package ca.etsmtl.taf.auth.controller;

import ca.etsmtl.taf.auth.services.AuthStrategy;
import ca.etsmtl.taf.auth.payload.request.RefreshTokenRequest;
import ca.etsmtl.taf.auth.payload.request.ValidateTokenRequest;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import ca.etsmtl.taf.auth.services.JwtService;
import ca.etsmtl.taf.auth.services.UserOldService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.etsmtl.taf.auth.payload.request.SignupRequest;
import ca.etsmtl.taf.auth.payload.response.MessageResponse;
import ca.etsmtl.taf.auth.payload.request.LoginRequest;
import org.springframework.web.client.HttpClientErrorException;
import lombok.extern.slf4j.Slf4j;

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

    private final AuthStrategy authStrategy;

    @Autowired
    public AuthController(AuthStrategy authStrategy) {
        this.authStrategy = authStrategy;
    }


    @PostMapping("/signin")
    public ResponseEntity<JwtResponse> signin(@RequestBody @Valid LoginRequest authenticationRequest) throws Exception {
        log.info("User {} attempting sign-in…", authenticationRequest.getUsername());
        ResponseEntity<JwtResponse> resp = authStrategy.signin(authenticationRequest);
        log.info("User {} signed in successfully.", authenticationRequest.getUsername());
        return resp;
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Boolean> validateToken(@RequestBody @Valid ValidateTokenRequest validateTokenRequest) throws Exception {
        boolean valid = authStrategy.validateToken(validateTokenRequest).getBody();
        log.info("Token validation result: {}", valid);
        return ResponseEntity.ok(valid);
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest) throws Exception {
        // Strategy implements the try/catch details (e.g., HttpClientErrorException) so the controller stays thin.
        ResponseEntity<?> resp = authStrategy.refreshToken(refreshTokenRequest);
        log.info("Token refresh processed (status={})", resp.getStatusCode());
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
