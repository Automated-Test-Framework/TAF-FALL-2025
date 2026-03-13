package ca.etsmtl.taf.security.oauth2;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.etsmtl.taf.entity.ERole;
import ca.etsmtl.taf.entity.Role;
import ca.etsmtl.taf.entity.User;
import ca.etsmtl.taf.repository.RoleRepository;
import ca.etsmtl.taf.repository.UserRepository;
import ca.etsmtl.taf.security.jwt.JwtUtils;

import java.lang.reflect.Field;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2LoginSuccessHandler — Google login flow")
class OAuth2LoginSuccessHandlerTest {

    @InjectMocks
    private OAuth2LoginSuccessHandler handler;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("New Google user → creates user, generates JWT, redirects")
    void newGoogleUser_createsUserAndRedirects() throws Exception {
        setField(handler, "frontendRedirectUrl", "http://localhost:4200");

        // OAuth2 user attributes from Google
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google123");
        attributes.put("email", "newuser@gmail.com");
        attributes.put("name", "New User");

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(), attributes, "sub");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(oAuth2User);

        // No existing user found
        when(userRepository.findByGoogleId("google123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

        User savedUser = new User("New User", "newuser", "newuser@gmail.com", "google", "google123");
        savedUser.setId("id1");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        savedUser.setRoles(roles);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtUtils.generateJwtTokenForUsername("newuser")).thenReturn("mock.jwt.token");
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"accessToken\":\"mock.jwt.token\"}");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, auth);

        // Should redirect to frontend
        assertEquals(302, response.getStatus());
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith("http://localhost:4200/oauth2/callback?token=mock.jwt.token"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Existing user by googleId → reuses user, no new save")
    void existingGoogleUser_reusesUser() throws Exception {
        setField(handler, "frontendRedirectUrl", "http://localhost:4200");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google123");
        attributes.put("email", "existing@gmail.com");
        attributes.put("name", "Existing User");

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(), attributes, "sub");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(oAuth2User);

        // User found by googleId
        User existingUser = new User("Existing User", "existing", "existing@gmail.com", "google", "google123");
        existingUser.setId("id2");
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        existingUser.setRoles(roles);

        when(userRepository.findByGoogleId("google123")).thenReturn(Optional.of(existingUser));
        when(jwtUtils.generateJwtTokenForUsername("existing")).thenReturn("existing.jwt.token");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, auth);

        assertEquals(302, response.getStatus());
        // Should NOT save a new user since they already exist
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Existing user by email → links Google account")
    void existingEmailUser_linksGoogleAccount() throws Exception {
        setField(handler, "frontendRedirectUrl", "http://localhost:4200");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google456");
        attributes.put("email", "local@etsmtl.ca");
        attributes.put("name", "Local User");

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(), attributes, "sub");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(oAuth2User);

        // No user by googleId, but found by email
        when(userRepository.findByGoogleId("google456")).thenReturn(Optional.empty());

        User localUser = new User("Local User", "localuser", "local@etsmtl.ca", "encodedPass");
        localUser.setId("id3");
        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        localUser.setRoles(roles);

        when(userRepository.findByEmail("local@etsmtl.ca")).thenReturn(Optional.of(localUser));
        when(userRepository.save(any(User.class))).thenReturn(localUser);
        when(jwtUtils.generateJwtTokenForUsername("localuser")).thenReturn("linked.jwt.token");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, auth);

        // Should save user with updated googleId and provider
        verify(userRepository).save(argThat(user ->
                "google456".equals(user.getGoogleId()) && "google".equals(user.getProvider())));
    }

    @Test
    @DisplayName("New user with duplicate username → appends googleId suffix")
    void newUser_duplicateUsername_appendsSuffix() throws Exception {
        setField(handler, "frontendRedirectUrl", "http://localhost:4200");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google789");
        attributes.put("email", "dupuser@gmail.com");
        attributes.put("name", "Dup User");

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(), attributes, "sub");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(oAuth2User);

        when(userRepository.findByGoogleId("google789")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("dupuser@gmail.com")).thenReturn(Optional.empty());
        // Username "dupuser" already taken
        when(userRepository.existsByUsername("dupuser")).thenReturn(true);

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));

        User savedUser = new User("Dup User", "dupuser_ggoogle", "dupuser@gmail.com", "google", "google789");
        savedUser.setId("id4");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        savedUser.setRoles(roles);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtUtils.generateJwtTokenForUsername(anyString())).thenReturn("dup.jwt.token");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, auth);

        // Verify saved username has suffix (not "dupuser")
        verify(userRepository).save(argThat(user ->
                user.getUsername().startsWith("dupuser_g")));
    }

    @Test
    @DisplayName("New user with no ROLE_USER in DB → creates user with empty roles")
    void newUser_noRoleInDb_emptyRoles() throws Exception {
        setField(handler, "frontendRedirectUrl", "http://localhost:4200");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google999");
        attributes.put("email", "norole@gmail.com");
        attributes.put("name", "No Role User");

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(), attributes, "sub");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(oAuth2User);

        when(userRepository.findByGoogleId("google999")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("norole@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.existsByUsername("norole")).thenReturn(false);
        // No ROLE_USER in DB
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.empty());

        User savedUser = new User("No Role User", "norole", "norole@gmail.com", "google", "google999");
        savedUser.setId("id5");
        savedUser.setRoles(new HashSet<>());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(jwtUtils.generateJwtTokenForUsername("norole")).thenReturn("norole.jwt.token");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, auth);

        // User saved with empty roles
        verify(userRepository).save(argThat(user -> user.getRoles().isEmpty()));
    }

    @Test
    @DisplayName("ObjectMapper throws → userInfoBase64 is empty string, still redirects")
    void objectMapperFails_stillRedirects() throws Exception {
        setField(handler, "frontendRedirectUrl", "http://localhost:4200");

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google111");
        attributes.put("email", "fail@gmail.com");
        attributes.put("name", "Fail User");

        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.emptyList(), attributes, "sub");

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(oAuth2User);

        User existingUser = new User("Fail User", "failuser", "fail@gmail.com", "google", "google111");
        existingUser.setId("id6");
        existingUser.setRoles(new HashSet<>());
        when(userRepository.findByGoogleId("google111")).thenReturn(Optional.of(existingUser));

        when(jwtUtils.generateJwtTokenForUsername("failuser")).thenReturn("fail.jwt.token");
        // ObjectMapper throws
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("serialize error"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, auth);

        // Should still redirect, with empty userInfo
        assertEquals(302, response.getStatus());
        String redirectUrl = response.getRedirectedUrl();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("userInfo="));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
