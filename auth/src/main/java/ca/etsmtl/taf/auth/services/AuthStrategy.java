package ca.etsmtl.taf.auth.services;

import ca.etsmtl.taf.auth.payload.request.*;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import ca.etsmtl.taf.auth.payload.response.MessageResponse;
import org.springframework.http.ResponseEntity;

public interface AuthStrategy {
    ResponseEntity<JwtResponse> signin(LoginRequest request) throws Exception;
    ResponseEntity<MessageResponse> signup(SignupRequest request);
    ResponseEntity<Boolean> validateToken(ValidateTokenRequest request) throws Exception;
    ResponseEntity<?> refreshToken(RefreshTokenRequest request) throws Exception;
}
