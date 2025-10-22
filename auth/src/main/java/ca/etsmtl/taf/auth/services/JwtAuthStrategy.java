package ca.etsmtl.taf.auth.services;

import ca.etsmtl.taf.auth.payload.request.*;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import ca.etsmtl.taf.auth.payload.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

@Service
@RequiredArgsConstructor
public class JwtAuthStrategy implements AuthStrategy {

    private final JwtService jwtService;
    private final UserOldService userService;

    @Override
    public ResponseEntity<JwtResponse> signin(LoginRequest request) throws Exception {
        JwtResponse response = jwtService.createJwtToken(request);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MessageResponse> signup(SignupRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username already taken!"));
        }
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email already in use!"));
        }
        userService.save(request);
        return ResponseEntity.ok(new MessageResponse("Inscription Réussie.!"));
    }

    @Override
    public ResponseEntity<Boolean> validateToken(ValidateTokenRequest request) throws Exception {
        boolean valid = jwtService.validateJwtToken(request);
        return ResponseEntity.ok(valid);
    }

    @Override
    public ResponseEntity<?> refreshToken(RefreshTokenRequest request) throws Exception {
        try {
            JwtResponse response = jwtService.refreshJwtToken(request);
            return ResponseEntity.ok(response);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getMessage());
        }
    }
}
