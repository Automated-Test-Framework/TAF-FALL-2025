package ca.etsmtl.taf.auth.services;

import ca.etsmtl.taf.auth.entity.User;
import ca.etsmtl.taf.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ca.etsmtl.taf.auth.model.CustomUserDetails;

import java.util.Optional;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //System.out.println("2:: Try Connection from " + username);
        Optional<User> user = userRepository.findByUsernameOrEmail(username, username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username or email : " + username);
        }
        return CustomUserDetails.build(user.get());
        //System.out.println("3:: Try Connection from " + user.get().getUsername() + " / " + user.get().getPassword());
        // this.log.debug("2:: Try Connection from {} / {}", user.get().getUsername(), user.get().getPassword());
        //return new org.springframework.security.core.userdetails.User(user.get().getUsername(), user.get().getPassword(), new ArrayList<>());
    }
}