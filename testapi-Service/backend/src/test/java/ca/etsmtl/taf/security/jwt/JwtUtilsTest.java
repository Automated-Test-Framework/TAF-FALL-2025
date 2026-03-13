package ca.etsmtl.taf.security.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ca.etsmtl.taf.security.services.UserDetailsImpl;

@DisplayName("JwtUtils — JWT token generation & validation")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    private static final String SECRET = "testSecretKeyThatIsLongEnoughForHS512AlgorithmToWork1234567890";
    private static final int EXPIRATION_MS = 86400000; // 24h
    private static final long REFRESH_EXPIRATION_MS = 604800000L; // 7 days

    @BeforeEach
    void setUp() throws Exception {
        jwtUtils = new JwtUtils();
        setField(jwtUtils, "jwtSecret", SECRET);
        setField(jwtUtils, "jwtExpirationMs", EXPIRATION_MS);
        setField(jwtUtils, "jwtRefreshExpirationMs", REFRESH_EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateJwtToken creates a valid token from Authentication")
    void generateJwtToken_shouldReturnValidToken() {
        Authentication auth = createAuth("testuser");
        String token = jwtUtils.generateJwtToken(auth);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    @DisplayName("generateJwtTokenForUsername creates a valid token from username string")
    void generateJwtTokenForUsername_shouldReturnValidToken() {
        String token = jwtUtils.generateJwtTokenForUsername("googleuser");

        assertNotNull(token);
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("googleuser", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    @DisplayName("getUserNameFromJwtToken extracts the correct username")
    void getUserNameFromJwtToken_shouldExtractUsername() {
        Authentication auth = createAuth("equipe3");
        String token = jwtUtils.generateJwtToken(auth);

        assertEquals("equipe3", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    @DisplayName("validateJwtToken rejects null token")
    void validateJwtToken_null_shouldReturnFalse() {
        assertFalse(jwtUtils.validateJwtToken(null));
    }

    @Test
    @DisplayName("validateJwtToken rejects empty token")
    void validateJwtToken_empty_shouldReturnFalse() {
        assertFalse(jwtUtils.validateJwtToken(""));
    }

    @Test
    @DisplayName("validateJwtToken rejects malformed token")
    void validateJwtToken_malformed_shouldReturnFalse() {
        assertFalse(jwtUtils.validateJwtToken("not.a.jwt"));
    }

    @Test
    @DisplayName("validateJwtToken rejects token signed with wrong key")
    void validateJwtToken_wrongKey_shouldReturnFalse() throws Exception {
        // Generate with different secret
        JwtUtils otherJwtUtils = new JwtUtils();
        setField(otherJwtUtils, "jwtSecret", "aCompletelyDifferentSecretKeyForTestingPurposes12345678");
        setField(otherJwtUtils, "jwtExpirationMs", EXPIRATION_MS);

        String token = otherJwtUtils.generateJwtTokenForUsername("hacker");
        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    @DisplayName("validateJwtToken rejects expired token")
    void validateJwtToken_expired_shouldReturnFalse() throws Exception {
        JwtUtils expiredJwtUtils = new JwtUtils();
        setField(expiredJwtUtils, "jwtSecret", SECRET);
        setField(expiredJwtUtils, "jwtExpirationMs", -1000); // already expired
        setField(expiredJwtUtils, "jwtRefreshExpirationMs", REFRESH_EXPIRATION_MS);

        String token = expiredJwtUtils.generateJwtTokenForUsername("expireduser");
        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    @DisplayName("generateRefreshToken creates a valid token with longer expiry")
    void generateRefreshToken_shouldReturnValidToken() {
        String token = jwtUtils.generateRefreshToken("testuser");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtUtils.validateJwtToken(token));
        assertEquals("testuser", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    @DisplayName("getUserNameFromExpiredJwtToken extracts username from expired token")
    void getUserNameFromExpiredJwtToken_shouldExtractUsername() throws Exception {
        JwtUtils expiredJwtUtils = new JwtUtils();
        setField(expiredJwtUtils, "jwtSecret", SECRET);
        setField(expiredJwtUtils, "jwtExpirationMs", -1000); // already expired
        setField(expiredJwtUtils, "jwtRefreshExpirationMs", REFRESH_EXPIRATION_MS);

        String expiredToken = expiredJwtUtils.generateJwtTokenForUsername("expireduser");

        // The token is expired, but we should still extract the username
        String username = jwtUtils.getUserNameFromExpiredJwtToken(expiredToken);
        assertEquals("expireduser", username);
    }

    @Test
    @DisplayName("getUserNameFromExpiredJwtToken works with valid (non-expired) token too")
    void getUserNameFromExpiredJwtToken_validToken_shouldExtractUsername() {
        String token = jwtUtils.generateJwtTokenForUsername("activeuser");
        String username = jwtUtils.getUserNameFromExpiredJwtToken(token);
        assertEquals("activeuser", username);
    }

    // --- helpers ---

    private Authentication createAuth(String username) {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                "id123", "Full Name", username, username + "@test.ca", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
