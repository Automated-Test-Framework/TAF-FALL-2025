package ca.etsmtl.taf.auth.services;

import ca.etsmtl.taf.auth.payload.request.LoginRequest;
import ca.etsmtl.taf.auth.payload.request.RefreshTokenRequest;
import ca.etsmtl.taf.auth.payload.request.SignupRequest;
import ca.etsmtl.taf.auth.payload.request.ValidateTokenRequest;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import ca.etsmtl.taf.auth.payload.response.MessageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthStrategyTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserOldService userService;

    @InjectMocks
    private JwtAuthStrategy jwtAuthStrategy;

    @Test
    void signin_returnsJwtResponse() throws Exception {
        LoginRequest req = new LoginRequest();
        JwtResponse expected = new JwtResponse();
        when(jwtService.createJwtToken(any(LoginRequest.class))).thenReturn(expected);

        ResponseEntity<JwtResponse> response = jwtAuthStrategy.signin(req);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(jwtService, times(1)).createJwtToken(req);
    }

    @Test
    void signup_usernameTaken_returnsBadRequest() {
        SignupRequest req = new SignupRequest();
        req.setUsername("existingUser");
        lenient().when(userService.existsByUsername(anyString())).thenReturn(true);

        ResponseEntity<MessageResponse> response = jwtAuthStrategy.signup(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Username already taken"));
        verify(userService, never()).save(any());
    }

    @Test
    void signup_emailInUse_returnsBadRequest() {
        SignupRequest req = new SignupRequest();
        req.setEmail("existing@example.com");
        lenient().when(userService.existsByUsername(anyString())).thenReturn(false);
        lenient().when(userService.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<MessageResponse> response = jwtAuthStrategy.signup(req);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Email already in use"));
        verify(userService, never()).save(any());
    }

    @Test
    void signup_success_savesUserAndReturnsOk() {
        SignupRequest req = new SignupRequest();
        lenient().when(userService.existsByUsername(anyString())).thenReturn(false);
        lenient().when(userService.existsByEmail(anyString())).thenReturn(false);

        ResponseEntity<MessageResponse> response = jwtAuthStrategy.signup(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("Inscription Réussie"));
        verify(userService, times(1)).save(req);
    }

    @Test
    void validateToken_true() throws Exception {
        ValidateTokenRequest req = new ValidateTokenRequest();
        when(jwtService.validateJwtToken(any(ValidateTokenRequest.class))).thenReturn(true);

        ResponseEntity<Boolean> response = jwtAuthStrategy.validateToken(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody());
        verify(jwtService, times(1)).validateJwtToken(req);
    }

    @Test
    void validateToken_false() throws Exception {
        ValidateTokenRequest req = new ValidateTokenRequest();
        when(jwtService.validateJwtToken(any(ValidateTokenRequest.class))).thenReturn(false);

        ResponseEntity<Boolean> response = jwtAuthStrategy.validateToken(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody());
        verify(jwtService, times(1)).validateJwtToken(req);
    }

    @Test
    void refreshToken_success_returnsJwtResponse() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        JwtResponse expected = new JwtResponse();
        when(jwtService.refreshJwtToken(any(RefreshTokenRequest.class))).thenReturn(expected);

        ResponseEntity<?> response = jwtAuthStrategy.refreshToken(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(jwtService, times(1)).refreshJwtToken(req);
    }

    @Test
    void refreshToken_httpClientError_returnsStatusAndMessage() throws Exception {
        RefreshTokenRequest req = new RefreshTokenRequest();
        HttpClientErrorException ex = new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        when(jwtService.refreshJwtToken(any(RefreshTokenRequest.class))).thenThrow(ex);

        ResponseEntity<?> response = jwtAuthStrategy.refreshToken(req);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        // body is the exception message
        assertTrue(response.getBody().toString().contains("Unauthorized"));
        verify(jwtService, times(1)).refreshJwtToken(req);
    }
}