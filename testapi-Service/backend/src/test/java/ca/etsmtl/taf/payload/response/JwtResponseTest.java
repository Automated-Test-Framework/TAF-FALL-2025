package ca.etsmtl.taf.payload.response;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("JwtResponse — JWT response DTO")
class JwtResponseTest {

    @Test
    @DisplayName("Constructor sets all fields correctly")
    void constructor_setsAllFields() {
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_ADMIN");

        JwtResponse response = new JwtResponse(
                "jwt.token.here", "id1", "Equipe 3", "equipe3", "equipe3@etsmtl.ca", roles);

        assertEquals("jwt.token.here", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("id1", response.getId());
        assertEquals("Equipe 3", response.getFullName());
        assertEquals("equipe3", response.getUsername());
        assertEquals("equipe3@etsmtl.ca", response.getEmail());
        assertEquals(roles, response.getRoles());
    }

    @Test
    @DisplayName("Setters update fields")
    void setters_updateFields() {
        JwtResponse response = new JwtResponse(
                "old", "id1", "Old", "old", "old@e.ca", List.of("ROLE_USER"));

        response.setAccessToken("new.token");
        response.setTokenType("Custom");
        response.setId("id2");
        response.setFullName("New");
        response.setUsername("new");
        response.setEmail("new@e.ca");

        assertEquals("new.token", response.getAccessToken());
        assertEquals("Custom", response.getTokenType());
        assertEquals("id2", response.getId());
        assertEquals("New", response.getFullName());
        assertEquals("new", response.getUsername());
        assertEquals("new@e.ca", response.getEmail());
    }

    @Test
    @DisplayName("Default tokenType is 'Bearer'")
    void defaultTokenType_isBearer() {
        JwtResponse response = new JwtResponse(
                "t", "id", "F", "u", "e@e.ca", List.of());

        assertEquals("Bearer", response.getTokenType());
    }
}
