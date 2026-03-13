package ca.etsmtl.taf.security.oauth2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import ca.etsmtl.taf.entity.ERole;
import ca.etsmtl.taf.entity.Role;
import ca.etsmtl.taf.entity.User;
import ca.etsmtl.taf.repository.RoleRepository;
import ca.etsmtl.taf.repository.UserRepository;
import ca.etsmtl.taf.security.jwt.JwtUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles successful Google OAuth2 login.
 * Creates or finds the user in MongoDB, generates a JWT token,
 * and redirects to the frontend with the token as a query parameter.
 */
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${taf.app.oauth2FrontendRedirectUrl:http://localhost:4200}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("OAuth2 login success for Google user: {} ({})", name, email);

        // Find existing user by googleId, or by email, or create a new one
        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existingUser -> {
                            // Link Google account to existing local user
                            existingUser.setGoogleId(googleId);
                            existingUser.setProvider("google");
                            return userRepository.save(existingUser);
                        })
                        .orElseGet(() -> {
                            // Create a brand new user from Google info
                            String username = email.split("@")[0]; // Use email prefix as username
                            // Ensure username uniqueness
                            if (userRepository.existsByUsername(username)) {
                                username = username + "_g" + googleId.substring(0, Math.min(googleId.length(), 6));
                            }
                            User newUser = new User(name, username, email, "google", googleId);

                            // Assign default ROLE_USER
                            Set<Role> roles = new HashSet<>();
                            roles.add(roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role ROLE_USER not found in database")));
                            newUser.setRoles(roles);

                            return userRepository.save(newUser);
                        })
                );

        // Generate JWT access token and refresh token for this user
        String jwtToken = jwtUtils.generateJwtTokenForUsername(user.getUsername());
        String refreshToken = jwtUtils.generateRefreshToken(user.getUsername());

        // Build user info for frontend
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toList());

        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("accessToken", jwtToken);
        userInfo.put("refreshToken", refreshToken);
        userInfo.put("id", user.getId());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("roles", roles);

        String userInfoBase64;
        try {
            String json = objectMapper.writeValueAsString(userInfo);
            userInfoBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Failed to encode user info", e);
            userInfoBase64 = "";
        }

        // Redirect to frontend with JWT token, refresh token, and user info
        String redirectUrl = frontendRedirectUrl + "/oauth2/callback?token=" + jwtToken + "&refreshToken=" + refreshToken + "&userInfo=" + userInfoBase64;
        log.info("Redirecting OAuth2 user to: {}", frontendRedirectUrl + "/oauth2/callback?token=***");

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
