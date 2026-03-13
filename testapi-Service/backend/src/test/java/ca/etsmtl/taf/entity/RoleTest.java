package ca.etsmtl.taf.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Role — entity tests")
class RoleTest {

    @Test
    @DisplayName("No-arg constructor creates Role with null fields")
    void noArgConstructor() {
        Role role = new Role();
        assertNull(role.getId());
        assertNull(role.getName());
    }

    @Test
    @DisplayName("Parameterized constructor sets name correctly")
    void parameterizedConstructor() {
        Role role = new Role(ERole.ROLE_ADMIN);
        assertEquals(ERole.ROLE_ADMIN, role.getName());
        assertNull(role.getId());
    }

    @Test
    @DisplayName("Getters and setters work correctly")
    void gettersAndSetters() {
        Role role = new Role();

        role.setId("abc123");
        role.setName(ERole.ROLE_USER);

        assertEquals("abc123", role.getId());
        assertEquals(ERole.ROLE_USER, role.getName());
    }
}
