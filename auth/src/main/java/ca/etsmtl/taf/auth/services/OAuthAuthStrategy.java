package ca.etsmtl.taf.auth.services;

import ca.etsmtl.taf.auth.model.CustomUserDetails;
import ca.etsmtl.taf.auth.payload.request.*;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import ca.etsmtl.taf.auth.payload.response.MessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OAuthAuthStrategy:
 * Used when feature.auth.mode=oauth
 *
 * This mode simulates OAuth2 authentication for consistency.
 * It uses AuthenticationManager and UserOldService to validate
 * credentials and retrieve user info, but does not issue real tokens.
 */
@Service
@RequiredArgsConstructor
public class OAuthAuthStrategy implements AuthStrategy {

    private final AuthenticationManager authenticationManager;
    private final UserOldService userService;

    @Override
    public ResponseEntity<JwtResponse> signin(LoginRequest request) {
        // Authenticate credentials (simulating OAuth2 provider login)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Store authentication in the security context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Extract authenticated user info
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        JwtResponse response = new JwtResponse(
                "OAUTH2-TOKEN-SIMULATED",     // placeholder OAuth2 access token
                "OAUTH2-REFRESH-SIMULATED",   // placeholder refresh token
                "Bearer",                     // type
                userDetails.getId(),
                userDetails.getFullName(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles != null ? roles : Collections.emptyList()
        );

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<MessageResponse> signup(SignupRequest request) {
        // Same signup logic as BasicAuthStrategy
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username already taken!"));
        }
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email already in use!"));
        }

        userService.save(request);
        return ResponseEntity.ok(new MessageResponse("User registered successfully using OAuth2 mode."));
    }

    @Override
    public ResponseEntity<Boolean> validateToken(ValidateTokenRequest request) {
        // Simulate token validation (would normally call OAuth provider)
        return ResponseEntity.ok(true);
    }

    @Override
    public ResponseEntity<?> refreshToken(RefreshTokenRequest request) {
        // Simulate token refresh
        JwtResponse response = new JwtResponse(
                "OAUTH2-TOKEN-REFRESHED",
                "OAUTH2-REFRESH-UPDATED",
                "Bearer",
                null,
                null,
                null,
                null,
                Collections.emptyList()
        );
        return ResponseEntity.ok(response);
    }
}
