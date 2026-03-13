package ca.etsmtl.taf.security.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ca.etsmtl.taf.entity.ERole;
import ca.etsmtl.taf.entity.Role;
import ca.etsmtl.taf.entity.User;
import ca.etsmtl.taf.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl - loadUserByUsername")
class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl service;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("loadUserByUsername - user found -> returns UserDetailsImpl")
    void loadUserByUsername_found() {
        User user = new User("Equipe 3", "equipe3", "equipe3@etsmtl.ca", "encodedPass");
        user.setId("id1");

        Role userRole = new Role();
        userRole.setName(ERole.ROLE_USER);
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        when(userRepository.findByUsername("equipe3")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("equipe3");

        assertEquals("equipe3", result.getUsername());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("loadUserByUsername - user not found -> throws UsernameNotFound")
    void loadUserByUsername_notFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                service.loadUserByUsername("nonexistent"));
    }
}
