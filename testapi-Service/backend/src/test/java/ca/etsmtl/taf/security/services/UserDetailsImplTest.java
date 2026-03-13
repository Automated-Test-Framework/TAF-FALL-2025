package ca.etsmtl.taf.security.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ca.etsmtl.taf.entity.ERole;
import ca.etsmtl.taf.entity.Role;
import ca.etsmtl.taf.entity.User;

@DisplayName("UserDetailsImpl — user details wrapper")
class UserDetailsImplTest {

    @Test
    @DisplayName("build() creates UserDetailsImpl from User entity")
    void build_fromUserEntity() {
        User user = new User("Equipe 3", "equipe3", "equipe3@etsmtl.ca", "encodedPass");
        user.setId("id1");

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertEquals("id1", details.getId());
        assertEquals("Equipe 3", details.getFullName());
        assertEquals("equipe3", details.getUsername());
        assertEquals("equipe3@etsmtl.ca", details.getEmail());
        assertEquals("encodedPass", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("All UserDetails boolean methods return true")
    void accountStatusMethods_allReturnTrue() {
        UserDetailsImpl details = new UserDetailsImpl(
                "id", "Full", "user", "e@e.ca", "p", Collections.emptyList());

        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }

    @Test
    @DisplayName("equals() — same id → true")
    void equals_sameId_true() {
        UserDetailsImpl u1 = new UserDetailsImpl("id1", "A", "a", "a@a.ca", "p", Collections.emptyList());
        UserDetailsImpl u2 = new UserDetailsImpl("id1", "B", "b", "b@b.ca", "q", Collections.emptyList());

        assertEquals(u1, u2);
    }

    @Test
    @DisplayName("equals() — different id → false")
    void equals_differentId_false() {
        UserDetailsImpl u1 = new UserDetailsImpl("id1", "A", "a", "a@a.ca", "p", Collections.emptyList());
        UserDetailsImpl u2 = new UserDetailsImpl("id2", "A", "a", "a@a.ca", "p", Collections.emptyList());

        assertNotEquals(u1, u2);
    }

    @Test
    @DisplayName("equals() — null → false")
    void equals_null_false() {
        UserDetailsImpl u1 = new UserDetailsImpl("id1", "A", "a", "a@a.ca", "p", Collections.emptyList());

        assertFalse(u1.equals(null));
    }

    @Test
    @DisplayName("equals() — different class → false")
    void equals_differentClass_false() {
        UserDetailsImpl u1 = new UserDetailsImpl("id1", "A", "a", "a@a.ca", "p", Collections.emptyList());

        assertFalse(u1.equals("not a UserDetailsImpl"));
    }

    @Test
    @DisplayName("equals() — same reference → true")
    void equals_sameReference_true() {
        UserDetailsImpl u1 = new UserDetailsImpl("id1", "A", "a", "a@a.ca", "p", Collections.emptyList());

        assertEquals(u1, u1);
    }

    @Test
    @DisplayName("Multiple roles mapped correctly")
    void build_multipleRoles() {
        User user = new User("Admin", "admin", "admin@ets.ca", "pass");
        user.setId("id2");

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        roles.add(adminRole);
        user.setRoles(roles);

        UserDetailsImpl details = UserDetailsImpl.build(user);

        assertEquals(2, details.getAuthorities().size());
        assertTrue(details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .allMatch(a -> a.equals("ROLE_USER") || a.equals("ROLE_ADMIN")));
    }
}
