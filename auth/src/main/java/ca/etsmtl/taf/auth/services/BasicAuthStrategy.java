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

@Service
@RequiredArgsConstructor
public class BasicAuthStrategy implements AuthStrategy {

    private final AuthenticationManager authenticationManager;
    private final UserOldService userService;

    @Override
    public ResponseEntity<JwtResponse> signin(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        JwtResponse response = new JwtResponse(
                "BASIC-AUTH-NO-TOKEN",
                null,
                "Bearer",
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
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Username already taken!"));
        }
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Email already in use!"));
        }

        userService.save(request);
        return ResponseEntity.ok(new MessageResponse("User registered successfully using Basic Auth."));
    }

    @Override
    public ResponseEntity<Boolean> validateToken(ValidateTokenRequest request) {
        return ResponseEntity.ok(true);
    }

    @Override
    public ResponseEntity<?> refreshToken(RefreshTokenRequest request) {
        return ResponseEntity.ok(new MessageResponse("Basic Auth does not use refresh tokens."));
    }
}
