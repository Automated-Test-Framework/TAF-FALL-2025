package ca.etsmtl.taf.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ca.etsmtl.taf.controller.AuthController;
import ca.etsmtl.taf.controller.OAuth2Controller;
import ca.etsmtl.taf.controller.TestApiController;
import ca.etsmtl.taf.repository.RoleRepository;
import ca.etsmtl.taf.repository.UserRepository;
import ca.etsmtl.taf.security.jwt.AuthEntryPointJwt;
import ca.etsmtl.taf.security.jwt.AuthTokenFilter;
import ca.etsmtl.taf.security.jwt.JwtUtils;
import ca.etsmtl.taf.security.oauth2.OAuth2LoginSuccessHandler;
import ca.etsmtl.taf.security.services.UserDetailsServiceImpl;
import ca.etsmtl.taf.service.UserRegistrationService;

@WebMvcTest(controllers = {OAuth2Controller.class, AuthController.class, TestApiController.class})
@Import({WebSecurityConfig.class, AuthEntryPointJwt.class})
@DisplayName("WebSecurityConfig — security rules integration tests")
class WebSecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // Dependencies of WebSecurityConfig
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    // Dependencies of controllers
    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RoleRepository roleRepository;

    @MockitoBean
    private JwtUtils jwtUtils;

    @MockitoBean
    private PasswordEncoder encoder;

    @MockitoBean
    private UserRegistrationService userRegistrationService;

    // ==================== Public endpoints → 200 (no auth) ====================

    @Test
    @DisplayName("GET /api/oauth2/login-url — public, returns 200")
    void publicEndpoint_oauth2LoginUrl_returns200() throws Exception {
        mockMvc.perform(get("/api/oauth2/login-url"))
                .andExpect(status().isOk());
    }

    // ==================== Protected endpoints → 401 (no auth) ====================

    @Test
    @DisplayName("POST /api/testapi/checkApi — no auth → 401")
    void protectedEndpoint_testapi_returns401() throws Exception {
        mockMvc.perform(post("/api/testapi/checkApi")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"method\":\"GET\",\"apiUrl\":\"http://example.com\",\"statusCode\":200}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/unknown — no auth → 401")
    void protectedEndpoint_unknown_returns401() throws Exception {
        mockMvc.perform(get("/api/unknown"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Bean method unit tests ====================

    @Test
    @DisplayName("passwordEncoder returns BCryptPasswordEncoder")
    void passwordEncoder_returnsBCryptPasswordEncoder() {
        WebSecurityConfig config = new WebSecurityConfig();
        assertInstanceOf(BCryptPasswordEncoder.class, config.passwordEncoder());
    }

    @Test
    @DisplayName("authenticationJwtTokenFilter returns AuthTokenFilter")
    void authenticationJwtTokenFilter_returnsAuthTokenFilter() {
        WebSecurityConfig config = new WebSecurityConfig();
        assertInstanceOf(AuthTokenFilter.class, config.authenticationJwtTokenFilter());
    }

    @Test
    @DisplayName("authenticationManager delegates to AuthenticationConfiguration")
    void authenticationManager_delegatesToConfig() throws Exception {
        WebSecurityConfig config = new WebSecurityConfig();
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(authConfig.getAuthenticationManager()).thenReturn(mockManager);

        AuthenticationManager result = config.authenticationManager(authConfig);
        assertSame(mockManager, result);
    }
}
