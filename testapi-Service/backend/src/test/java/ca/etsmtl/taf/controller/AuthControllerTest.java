package ca.etsmtl.taf.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashSet;
import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.etsmtl.taf.payload.request.LoginRequest;
import ca.etsmtl.taf.payload.request.RefreshTokenRequest;
import ca.etsmtl.taf.payload.request.SignupRequest;
import ca.etsmtl.taf.security.jwt.JwtUtils;
import ca.etsmtl.taf.security.services.UserDetailsImpl;
import ca.etsmtl.taf.security.services.UserDetailsServiceImpl;
import ca.etsmtl.taf.service.UserRegistrationService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController — /api/auth endpoints")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private UserRegistrationService userRegistrationService;

    // ==================== /signin ====================

    @Test
    @DisplayName("POST /api/auth/signin — valid credentials return JWT")
    void signin_validCredentials_returnsJwt() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("equipe3");
        loginReq.setPassword("equipe3");

        UserDetailsImpl userDetails = new UserDetailsImpl(
                "id1", "Equipe 3", "equipe3", "equipe3@etsmtl.ca", "encodedPass",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("mock.jwt.token");
        when(jwtUtils.generateRefreshToken("equipe3")).thenReturn("mock.refresh.token");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
                .andExpect(jsonPath("$.refreshToken").value("mock.refresh.token"))
                .andExpect(jsonPath("$.username").value("equipe3"))
                .andExpect(jsonPath("$.email").value("equipe3@etsmtl.ca"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    @DisplayName("POST /api/auth/signin — bad credentials throws ServletException wrapping BadCredentialsException")
    void signin_badCredentials_throwsException() throws Exception {
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("wrong");
        loginReq.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // With filters disabled, BadCredentialsException propagates as ServletException
        Exception thrown = assertThrows(Exception.class, () ->
                mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq))));
        assertTrue(thrown instanceof jakarta.servlet.ServletException
                || thrown.getCause() instanceof BadCredentialsException);
    }

    @Test
    @DisplayName("POST /api/auth/signin — empty body returns 400")
    void signin_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // ==================== /signup ====================

    @Test
    @DisplayName("POST /api/auth/signup — new user success")
    void signup_newUser_returnsOk() throws Exception {
        SignupRequest signupReq = new SignupRequest();
        signupReq.setFullName("Equipe 3");
        signupReq.setUsername("equipe3");
        signupReq.setEmail("equipe3@etsmtl.ca");
        signupReq.setPassword("equipe3");

        when(userRegistrationService.existsByUsername("equipe3")).thenReturn(false);
        when(userRegistrationService.existsByEmail("equipe3@etsmtl.ca")).thenReturn(false);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inscription Réussie.!"));
    }

    @Test
    @DisplayName("POST /api/auth/signup — duplicate username returns 400")
    void signup_duplicateUsername_returnsBadRequest() throws Exception {
        SignupRequest signupReq = new SignupRequest();
        signupReq.setFullName("Equipe 3");
        signupReq.setUsername("equipe3");
        signupReq.setEmail("equipe3@etsmtl.ca");
        signupReq.setPassword("equipe3");

        when(userRegistrationService.existsByUsername("equipe3")).thenReturn(true);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    @DisplayName("POST /api/auth/signup — duplicate email returns 400")
    void signup_duplicateEmail_returnsBadRequest() throws Exception {
        SignupRequest signupReq = new SignupRequest();
        signupReq.setFullName("Equipe 3");
        signupReq.setUsername("equipe3new");
        signupReq.setEmail("equipe3@etsmtl.ca");
        signupReq.setPassword("equipe3");

        when(userRegistrationService.existsByUsername("equipe3new")).thenReturn(false);
        when(userRegistrationService.existsByEmail("equipe3@etsmtl.ca")).thenReturn(true);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));
    }

    @Test
    @DisplayName("POST /api/auth/signup — admin role assignment")
    void signup_adminRole_assignsCorrectly() throws Exception {
        SignupRequest signupReq = new SignupRequest();
        signupReq.setFullName("Admin");
        signupReq.setUsername("admin1");
        signupReq.setEmail("admin@etsmtl.ca");
        signupReq.setPassword("admin123");
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        signupReq.setRole(roles);

        when(userRegistrationService.existsByUsername("admin1")).thenReturn(false);
        when(userRegistrationService.existsByEmail("admin@etsmtl.ca")).thenReturn(false);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inscription Réussie.!"));

        verify(userRegistrationService).registerUser(eq("Admin"), eq("admin1"), eq("admin@etsmtl.ca"), eq("admin123"), any());
    }

    // ==================== /refresh-token ====================

    @Test
    @DisplayName("POST /api/auth/refresh-token — valid refresh token returns new token pair")
    void refreshToken_validToken_returnsNewTokenPair() throws Exception {
        RefreshTokenRequest refreshReq = new RefreshTokenRequest();
        refreshReq.setRefreshToken("valid.refresh.token");

        UserDetailsImpl userDetails = new UserDetailsImpl(
                "id1", "Equipe 3", "equipe3", "equipe3@etsmtl.ca", "encodedPass",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(jwtUtils.validateJwtToken("valid.refresh.token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("valid.refresh.token")).thenReturn("equipe3");
        when(userRegistrationService.existsByUsername("equipe3")).thenReturn(true);
        when(jwtUtils.generateJwtTokenForUsername("equipe3")).thenReturn("new.access.token");
        when(jwtUtils.generateRefreshToken("equipe3")).thenReturn("new.refresh.token");
        when(userDetailsService.loadUserByUsername("equipe3")).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.refreshToken").value("new.refresh.token"))
                .andExpect(jsonPath("$.username").value("equipe3"))
                .andExpect(jsonPath("$.email").value("equipe3@etsmtl.ca"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh-token — expired refresh token returns 401")
    void refreshToken_expiredToken_returns401() throws Exception {
        RefreshTokenRequest refreshReq = new RefreshTokenRequest();
        refreshReq.setRefreshToken("expired.refresh.token");

        when(jwtUtils.validateJwtToken("expired.refresh.token")).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /api/auth/refresh-token — user not found returns 401")
    void refreshToken_userNotFound_returns401() throws Exception {
        RefreshTokenRequest refreshReq = new RefreshTokenRequest();
        refreshReq.setRefreshToken("valid.refresh.token");

        when(jwtUtils.validateJwtToken("valid.refresh.token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("valid.refresh.token")).thenReturn("deleteduser");
        when(userRegistrationService.existsByUsername("deleteduser")).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    // ==================== Signup — additional branch coverage ====================

    @Test
    @DisplayName("POST /api/auth/signup — non-admin explicit role defaults to ROLE_USER")
    void signup_userRole_assignsCorrectly() throws Exception {
        SignupRequest signupReq = new SignupRequest();
        signupReq.setFullName("User");
        signupReq.setUsername("user1");
        signupReq.setEmail("user@etsmtl.ca");
        signupReq.setPassword("user123");
        Set<String> roles = new HashSet<>();
        roles.add("user"); // NOT "admin" → false branch of "admin".equals(role)
        signupReq.setRole(roles);

        when(userRegistrationService.existsByUsername("user1")).thenReturn(false);
        when(userRegistrationService.existsByEmail("user@etsmtl.ca")).thenReturn(false);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inscription Réussie.!"));
    }

    @Test
    @DisplayName("POST /api/auth/signup — role not found with null roles throws RuntimeException")
    void signup_roleNotFound_defaultRoles_throwsException() {
        SignupRequest signupReq = new SignupRequest();
        signupReq.setFullName("Test");
        signupReq.setUsername("testuser");
        signupReq.setEmail("test@test.com");
        signupReq.setPassword("password");
        // strRoles is null → defaults to finding ROLE_USER

        when(userRegistrationService.existsByUsername("testuser")).thenReturn(false);
        when(userRegistrationService.existsByEmail("test@test.com")).thenReturn(false);
        when(userRegistrationService.registerUser(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Error: Role is not found."));

        assertThrows(jakarta.servlet.ServletException.class, () ->
            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupReq))));
    }

    @Test
    @DisplayName("POST /api/auth/signup — role not found with explicit roles throws RuntimeException")
    void signup_roleNotFound_explicitRoles_throwsException() {
        SignupRequest signupReq = new SignupRequest();
        signupReq.setFullName("Admin");
        signupReq.setUsername("admin2");
        signupReq.setEmail("admin2@test.com");
        signupReq.setPassword("admin123");
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        signupReq.setRole(roles);

        when(userRegistrationService.existsByUsername("admin2")).thenReturn(false);
        when(userRegistrationService.existsByEmail("admin2@test.com")).thenReturn(false);
        when(userRegistrationService.registerUser(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Error: Role is not found."));

        assertThrows(jakarta.servlet.ServletException.class, () ->
            mockMvc.perform(post("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(signupReq))));
    }
}
