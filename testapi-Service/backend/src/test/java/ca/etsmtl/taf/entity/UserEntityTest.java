package ca.etsmtl.taf.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("User & Role — entity tests")
class UserEntityTest {

    @Test
    @DisplayName("Local user constructor sets provider to 'local'")
    void localUserConstructor() {
        User user = new User("Full Name", "username", "email@ets.ca", "password");

        assertEquals("Full Name", user.getFullName());
        assertEquals("username", user.getUsername());
        assertEquals("email@ets.ca", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals("local", user.getProvider());
        assertNull(user.getGoogleId());
    }

    @Test
    @DisplayName("OAuth2 user constructor sets provider and googleId")
    void oauth2UserConstructor() {
        User user = new User("Google User", "guser", "guser@gmail.com", "google", "google123");

        assertEquals("Google User", user.getFullName());
        assertEquals("guser", user.getUsername());
        assertEquals("guser@gmail.com", user.getEmail());
        assertEquals("google", user.getProvider());
        assertEquals("google123", user.getGoogleId());
        // OAuth2 user gets a random UUID password
        assertNotNull(user.getPassword());
        assertFalse(user.getPassword().isEmpty());
    }

    @Test
    @DisplayName("Default constructor + setters")
    void defaultConstructorAndSetters() {
        User user = new User();
        user.setId("id1");
        user.setFullName("Test");
        user.setUsername("test");
        user.setEmail("test@ets.ca");
        user.setPassword("pass");
        user.setProvider("local");
        user.setGoogleId(null);

        assertEquals("id1", user.getId());
        assertEquals("Test", user.getFullName());
        assertEquals("test", user.getUsername());
        assertEquals("test@ets.ca", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals("local", user.getProvider());
        assertNull(user.getGoogleId());
    }

    @Test
    @DisplayName("Roles assignment")
    void rolesAssignment() {
        User user = new User();
        Role role = new Role();
        role.setName(ERole.ROLE_USER);

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().stream()
                .anyMatch(r -> r.getName() == ERole.ROLE_USER));
    }

    @Test
    @DisplayName("ERole enum has USER and ADMIN")
    void eRoleEnum() {
        assertEquals(2, ERole.values().length);
        assertNotNull(ERole.valueOf("ROLE_USER"));
        assertNotNull(ERole.valueOf("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Role entity getter/setter")
    void roleEntity() {
        Role role = new Role();
        role.setName(ERole.ROLE_ADMIN);

        assertEquals(ERole.ROLE_ADMIN, role.getName());
    }
}
