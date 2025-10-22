package ca.etsmtl.taf.auth.services;

import ca.etsmtl.taf.auth.model.CustomUserDetails;
import ca.etsmtl.taf.auth.payload.request.LoginRequest;
import ca.etsmtl.taf.auth.payload.request.RefreshTokenRequest;
import ca.etsmtl.taf.auth.payload.request.SignupRequest;
import ca.etsmtl.taf.auth.payload.request.ValidateTokenRequest;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import ca.etsmtl.taf.auth.payload.response.MessageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthAuthStrategyTest {

    @Test
    void signin() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        UserOldService userService = mock(UserOldService.class);
        Authentication authentication = mock(Authentication.class);

        CustomUserDetails userDetails = new CustomUserDetails(
                "1",
                "John Doe",
                "johndoe",
                "password",
                "john@example.com",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        OAuthAuthStrategy strategy = new OAuthAuthStrategy(authManager, userService);

        LoginRequest loginRequest = mock(LoginRequest.class);
        when(loginRequest.getUsername()).thenReturn("johndoe");
        when(loginRequest.getPassword()).thenReturn("password");

        ResponseEntity<JwtResponse> response = strategy.signin(loginRequest);

        try {
            assertNotNull(response);
            assertEquals(200, response.getStatusCodeValue());
            JwtResponse body = response.getBody();
            assertNotNull(body);
            assertEquals("OAUTH2-TOKEN-SIMULATED", body.getToken());
            assertEquals("johndoe", body.getUsername());
            assertEquals("John Doe", body.getFullName());
            assertEquals(List.of("ROLE_USER"), body.getRoles());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void signup_existingUsername_returnsBadRequest() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        UserOldService userService = mock(UserOldService.class);

        when(userService.existsByUsername("taken")).thenReturn(true);

        OAuthAuthStrategy strategy = new OAuthAuthStrategy(authManager, userService);

        SignupRequest req = mock(SignupRequest.class);
        lenient().when(req.getUsername()).thenReturn("taken");
        lenient().when(req.getEmail()).thenReturn("any@example.com");

        ResponseEntity<MessageResponse> response = strategy.signup(req);
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Username already taken"));
    }

    @Test
    void signup_success_registersUser() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        UserOldService userService = mock(UserOldService.class);

        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("new@example.com")).thenReturn(false);

        OAuthAuthStrategy strategy = new OAuthAuthStrategy(authManager, userService);

        SignupRequest req = mock(SignupRequest.class);
        when(req.getUsername()).thenReturn("newuser");
        when(req.getEmail()).thenReturn("new@example.com");

        ResponseEntity<MessageResponse> response = strategy.signup(req);
        assertEquals(200, response.getStatusCodeValue());
        verify(userService, times(1)).save(req);
    }

    @Test
    void validateToken() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        UserOldService userService = mock(UserOldService.class);

        OAuthAuthStrategy strategy = new OAuthAuthStrategy(authManager, userService);

        ValidateTokenRequest req = mock(ValidateTokenRequest.class);
        ResponseEntity<Boolean> response = strategy.validateToken(req);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());
    }

    @Test
    void refreshToken() {
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        UserOldService userService = mock(UserOldService.class);

        OAuthAuthStrategy strategy = new OAuthAuthStrategy(authManager, userService);

        RefreshTokenRequest req = mock(RefreshTokenRequest.class);
        ResponseEntity<?> response = strategy.refreshToken(req);

        assertEquals(200, response.getStatusCodeValue());
        Object body = response.getBody();
        assertTrue(body instanceof JwtResponse);
        JwtResponse jwt = (JwtResponse) body;
        assertEquals("OAUTH2-TOKEN-REFRESHED", jwt.getToken());
    }
}