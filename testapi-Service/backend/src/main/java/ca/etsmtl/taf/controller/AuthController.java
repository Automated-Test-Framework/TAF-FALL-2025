package ca.etsmtl.taf.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import ca.etsmtl.taf.entity.ERole;
import ca.etsmtl.taf.entity.Role;
import ca.etsmtl.taf.entity.User;
import ca.etsmtl.taf.payload.request.LoginRequest;
import ca.etsmtl.taf.payload.request.RefreshTokenRequest;
import ca.etsmtl.taf.payload.request.SignupRequest;
import ca.etsmtl.taf.payload.response.JwtResponse;
import ca.etsmtl.taf.payload.response.MessageResponse;
import ca.etsmtl.taf.repository.RoleRepository;
import ca.etsmtl.taf.repository.UserRepository;
import ca.etsmtl.taf.security.jwt.JwtUtils;
import ca.etsmtl.taf.security.services.UserDetailsImpl;
import ca.etsmtl.taf.security.services.UserDetailsServiceImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());

    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    return ResponseEntity.ok(new JwtResponse(jwt,
                         refreshToken,
                         userDetails.getId(), 
                         userDetails.getFullName(), 
                         userDetails.getUsername(), 
                         userDetails.getEmail(), 
                         roles));
  }

  /**
   * Refresh token endpoint.
   * Accepts a valid refresh token and returns a new access token + refresh token pair.
   * The old refresh token is invalidated by issuing a new one (rotation strategy).
   */
  @PostMapping("/refresh-token")
  public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    String requestRefreshToken = request.getRefreshToken();

    // Validate the refresh token
    if (!jwtUtils.validateJwtToken(requestRefreshToken)) {
      logger.warn("Refresh token validation failed");
      return ResponseEntity.status(401)
          .body(new MessageResponse("Error: Refresh token is invalid or expired. Please sign in again."));
    }

    // Extract username from the refresh token
    String username = jwtUtils.getUserNameFromJwtToken(requestRefreshToken);

    // Verify user still exists in the database
    if (!userRepository.existsByUsername(username)) {
      logger.warn("Refresh token refers to non-existent user: {}", username);
      return ResponseEntity.status(401)
          .body(new MessageResponse("Error: User not found. Please sign in again."));
    }

    // Generate new token pair (token rotation)
    String newAccessToken = jwtUtils.generateJwtTokenForUsername(username);
    String newRefreshToken = jwtUtils.generateRefreshToken(username);

    // Load user details for response
    UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
    List<String> roles = userDetails.getAuthorities().stream()
        .map(item -> item.getAuthority())
        .collect(Collectors.toList());

    logger.info("Token refreshed successfully for user: {}", username);

    return ResponseEntity.ok(new JwtResponse(newAccessToken,
                         newRefreshToken,
                         userDetails.getId(),
                         userDetails.getFullName(),
                         userDetails.getUsername(),
                         userDetails.getEmail(),
                         roles));
  }

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getFullName(),
    		signUpRequest.getUsername(), 
               signUpRequest.getEmail(),
               encoder.encode(signUpRequest.getPassword()));

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
    userRepository.save(user);

    return ResponseEntity.ok(new MessageResponse("Inscription Réussie.!"));
  }
}
