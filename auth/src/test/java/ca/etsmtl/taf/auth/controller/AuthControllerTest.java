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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthStrategy authStrategy;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserOldService userService;

    @Mock
    private TokenCache tokenCache;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController controller;

    @Test
    void greeting() {
        String g = controller.greeting();
        assertEquals("Hello from Auth Microservice!", g);
    }

    @Test
    void signin() throws Exception {
        LoginRequest req = mock(LoginRequest.class);
        when(req.getUsername()).thenReturn("johndoe");

        JwtResponse jwt = mock(JwtResponse.class);
        when(jwt.getToken()).thenReturn("token");
        lenient().when(jwt.getUsername()).thenReturn("johndoe");

        when(authStrategy.signin(req)).thenReturn(ResponseEntity.ok(jwt));

        // ensure the mocked TokenCache and JwtUtil are injected into the controller instance
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "tokenCache", tokenCache);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "jwtUtil", jwtUtil);

        ResponseEntity<JwtResponse> resp = controller.signin(req);
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCodeValue());
        assertSame(jwt, resp.getBody());

        verify(tokenCache, times(1)).update("johndoe", jwt, jwtUtil);
    }


    @Test
    void validateToken() throws Exception {
        ValidateTokenRequest req = mock(ValidateTokenRequest.class);
        when(authStrategy.validateToken(req)).thenReturn(ResponseEntity.ok(Boolean.TRUE));

        ResponseEntity<Boolean> resp = controller.validateToken(req);
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCodeValue());
        assertTrue(resp.getBody());
    }

    @Test
    void refreshToken() throws Exception {
        RefreshTokenRequest req = mock(RefreshTokenRequest.class);

        JwtResponse jwt = mock(JwtResponse.class);
        when(jwt.getToken()).thenReturn("token-1");

        // arrange: make authStrategy return the response without generic resolution issues
        doReturn(ResponseEntity.ok(jwt)).when(authStrategy).refreshToken(req);
        when(jwtUtil.extractUsername("token-1")).thenReturn("johndoe");

        // inject mocks into the controller so controller uses the mocked jwtUtil and tokenCache
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "tokenCache", tokenCache);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "jwtUtil", jwtUtil);

        ResponseEntity<?> resp = controller.refreshToken(req);
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCodeValue());
        assertSame(jwt, resp.getBody());

        verify(jwtUtil, times(1)).extractUsername("token-1");
        verify(tokenCache, times(1)).update("johndoe", jwt, jwtUtil);
    }

    @Test
    void registerUser() {
        SignupRequest req = mock(SignupRequest.class);
        when(req.getUsername()).thenReturn("newuser");

        MessageResponse msg = mock(MessageResponse.class);
        when(authStrategy.signup(req)).thenReturn(ResponseEntity.ok(msg));

        ResponseEntity<?> resp = controller.registerUser(req);
        assertNotNull(resp);
        assertEquals(200, resp.getStatusCodeValue());
        assertSame(msg, resp.getBody());
    }
}
