package ca.etsmtl.taf.service;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import ca.etsmtl.taf.entity.ERole;
import ca.etsmtl.taf.entity.Role;
import ca.etsmtl.taf.entity.User;
import ca.etsmtl.taf.repository.RoleRepository;
import ca.etsmtl.taf.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder encoder;

    public UserRegistrationService(UserRepository userRepository,
                                   RoleRepository roleRepository,
                                   PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User registerUser(String fullName, String username, String email,
                             String password, Set<String> strRoles) {
        User user = new User(fullName, username, email, encoder.encode(password));

        Set<Role> roles = resolveRoles(strRoles);
        user.setRoles(roles);
        return userRepository.save(user);
    }

    private Set<Role> resolveRoles(Set<String> strRoles) {
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                ERole currentRoleType = ERole.ROLE_USER;

                if ("admin".equals(role)) {
                    currentRoleType = ERole.ROLE_ADMIN;
                }

                Role currentRole = roleRepository.findByName(currentRoleType)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));

                roles.add(currentRole);
            });
        }

        return roles;
    }
}
