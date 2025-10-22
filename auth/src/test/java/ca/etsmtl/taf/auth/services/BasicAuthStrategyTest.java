package ca.etsmtl.taf.auth.services;

import ca.etsmtl.taf.auth.model.CustomUserDetails;
import ca.etsmtl.taf.auth.payload.request.LoginRequest;
import ca.etsmtl.taf.auth.payload.request.RefreshTokenRequest;
import ca.etsmtl.taf.auth.payload.request.SignupRequest;
import ca.etsmtl.taf.auth.payload.request.ValidateTokenRequest;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import ca.etsmtl.taf.auth.payload.response.MessageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BasicAuthStrategyTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    UserOldService userService;

    @InjectMocks
    BasicAuthStrategy strategy;

    @BeforeEach
    void setUp() {
    }

    @Test
    void signin() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("pass");

        Authentication authentication = mock(Authentication.class);

        // create a real CustomUserDetails instance instead of stubbing its getters
        CustomUserDetails userDetails = new CustomUserDetails("1", "Jane Doe", "jane",
                "jane.doe@example.com", "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        ResponseEntity<JwtResponse> response = strategy.signin(request);

        assertEquals(200, response.getStatusCodeValue());
        JwtResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("BASIC-AUTH-NO-TOKEN", body.getToken());
        assertEquals("jane", body.getUsername());
        assertTrue(body.getRoles().contains("ROLE_USER"));
    }

    @Test
    void signup() {
        SignupRequest request = new SignupRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");
        request.setPassword("pwd");

        when(userService.existsByUsername("newuser")).thenReturn(false);
        when(userService.existsByEmail("new@example.com")).thenReturn(false);

        ResponseEntity<MessageResponse> response = strategy.signup(request);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User registered successfully using Basic Auth.", response.getBody().getMessage());
        verify(userService, times(1)).save(request);
    }

    @Test
    void validateToken() {
        ValidateTokenRequest request = new ValidateTokenRequest();
        ResponseEntity<Boolean> response = strategy.validateToken(request);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody());
    }

    @Test
    void refreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        ResponseEntity<?> response = strategy.refreshToken(request);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MessageResponse);
        assertEquals("Basic Auth does not use refresh tokens.", ((MessageResponse) response.getBody()).getMessage());
    }
}