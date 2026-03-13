package ca.etsmtl.taf.user.services;
import ca.etsmtl.taf.user.entity.ERole;
import ca.etsmtl.taf.user.entity.Role;
import ca.etsmtl.taf.user.entity.User;
import ca.etsmtl.taf.user.payload.request.PasswordRequest;
import ca.etsmtl.taf.user.payload.request.SignupRequest;
import ca.etsmtl.taf.user.payload.request.UpdateRequest;
import ca.etsmtl.taf.user.repository.RoleRepository;
import ca.etsmtl.taf.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     *
     * @param username
     * @param signUpRequest
     * @return
     */
    public User update(String username, UpdateRequest signUpRequest) {
        // Update user's account
        User user = userRepository.findByUsername(username).orElseThrow();
        if(!signUpRequest.getFullName().isEmpty())
            user.setFullName(signUpRequest.getFullName());
        if(!signUpRequest.getEmail().isEmpty())
            user.setEmail(signUpRequest.getEmail());

        return userRepository.save(user);
    }



    /**
     *
     * @param username
     * @param passwordRequest
     * @return
     */
    public int updatePassword(String username, PasswordRequest passwordRequest) {
        // Update user's account
        User user = userRepository.findByUsername(username).orElseThrow();
        String oldPasswd = passwordEncoder.encode(passwordRequest.getOldPassword());
        String password = passwordEncoder.encode(passwordRequest.getPassword());
        if(!user.getPassword().equals(oldPasswd))
            return -1;
        if(user.getPassword().equals(password))
            return -2;
        user.setPassword(password);
        userRepository.save(user);
        return 0;
    }

    /**
     *
     * @param signUpRequest
     * @return
     */
    public User save(SignupRequest signUpRequest) {
        // Create new user's account
        User user = new User(signUpRequest.getFullName(),
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()));
        Set<String> strRoles = signUpRequest.getRole();
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

        user.setRoles(roles);

        return userRepository.save(user);
    }

    /**
     *
     * @param id
     * @return
     */
    public User findById(String id) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        return userRepository.findById(id).orElseThrow();
    }

    /**
     *
     * @param username
     * @return
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    /**
     *
     * @param username
     * @return
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     *
     * @param email
     * @return
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     *
     * @param email
     * @param id
     * @return
     */
    public boolean existsByEmailAndIdNot(String email, String id) {
        return userRepository.existsByEmailAndIdNot(email, id);
    }

}
