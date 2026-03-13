package ca.etsmtl.taf.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashSet;
import java.util.Collections;
import java.util.Optional;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.etsmtl.taf.entity.ERole;
import ca.etsmtl.taf.entity.Role;
import ca.etsmtl.taf.payload.request.LoginRequest;
import ca.etsmtl.taf.payload.request.SignupRequest;
import ca.etsmtl.taf.repository.RoleRepository;
import ca.etsmtl.taf.repository.UserRepository;
import ca.etsmtl.taf.security.jwt.JwtUtils;
import ca.etsmtl.taf.security.services.UserDetailsImpl;

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
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private PasswordEncoder encoder;

    @MockitoBean
    private JwtUtils jwtUtils;

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

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock.jwt.token"))
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

        when(userRepository.existsByUsername("equipe3")).thenReturn(false);
        when(userRepository.existsByEmail("equipe3@etsmtl.ca")).thenReturn(false);

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

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

        when(userRepository.existsByUsername("equipe3")).thenReturn(true);

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

        when(userRepository.existsByUsername("equipe3new")).thenReturn(false);
        when(userRepository.existsByEmail("equipe3@etsmtl.ca")).thenReturn(true);

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

        when(userRepository.existsByUsername("admin1")).thenReturn(false);
        when(userRepository.existsByEmail("admin@etsmtl.ca")).thenReturn(false);

        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inscription Réussie.!"));

        verify(userRepository).save(argThat(user -> user.getRoles().contains(adminRole)));
    }
}
