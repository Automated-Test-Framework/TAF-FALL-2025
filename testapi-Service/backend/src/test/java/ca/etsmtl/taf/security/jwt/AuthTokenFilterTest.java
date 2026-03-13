package ca.etsmtl.taf.security.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import ca.etsmtl.taf.security.services.UserDetailsImpl;
import ca.etsmtl.taf.security.services.UserDetailsServiceImpl;

import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthTokenFilter — JWT filter chain logic")
class AuthTokenFilterTest {

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Valid Bearer token sets SecurityContext authentication")
    void validToken_shouldSetAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtils.validateJwtToken(token)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(token)).thenReturn("equipe3");

        UserDetails userDetails = new UserDetailsImpl(
                "id1", "Equipe 3", "equipe3", "equipe3@etsmtl.ca", "pass",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        when(userDetailsService.loadUserByUsername("equipe3")).thenReturn(userDetails);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("equipe3", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    @DisplayName("No Authorization header → no authentication set, filter continues")
    void noHeader_shouldNotSetAuthentication() throws ServletException, IOException {
        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Invalid token → no authentication set, filter continues")
    void invalidToken_shouldNotSetAuthentication() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer invalid.token");

        when(jwtUtils.validateJwtToken("invalid.token")).thenReturn(false);

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    @DisplayName("Non-Bearer Authorization header → no authentication set")
    void nonBearerHeader_shouldNotSetAuthentication() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic dXNlcjpwYXNz");

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtils);
    }

    @Test
    @DisplayName("Exception during token processing → filter continues without auth")
    void exception_shouldContinueFilterChain() throws ServletException, IOException {
        request.addHeader("Authorization", "Bearer crash.token");

        when(jwtUtils.validateJwtToken("crash.token")).thenThrow(new RuntimeException("DB down"));

        authTokenFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        // Filter chain should still be called
    }
}
