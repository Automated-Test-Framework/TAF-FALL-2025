package ca.etsmtl.taf.auth.services;

import ca.etsmtl.taf.auth.jwt.JwtUtil;
import ca.etsmtl.taf.auth.model.CustomUserDetails;
import ca.etsmtl.taf.auth.payload.request.LoginRequest;
import ca.etsmtl.taf.auth.payload.request.RefreshTokenRequest;
import ca.etsmtl.taf.auth.payload.request.ValidateTokenRequest;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.Collectors;

import java.util.List;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class JwtService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    UserClient userClient;

    public JwtResponse createJwtToken(LoginRequest authenticationRequest) throws Exception {
        //System.out.println("1:: Try Connection from " + authenticationRequest.getUsername() + " / " + authenticationRequest.getPassword() + " / " + passwordEncoder.encode(authenticationRequest.getPassword()));
        // this.log.debug("1:: Try Connection from {} / {} / {}", authenticationRequest.getUsername(), authenticationRequest.getPassword(), passwordEncoder.encode(authenticationRequest.getPassword()));
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getUsername(), authenticationRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        //final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtUtil.generateToken(userDetails);

        final String refresh = jwtUtil.generateToken(userDetails);

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtResponse(token,
                refresh,
                userDetails.getId(),
                userDetails.getFullName(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles);
    }

    public boolean validateJwtToken(ValidateTokenRequest validateTokenRequest) throws Exception {
        try{
            return jwtUtil.validateToken(validateTokenRequest.getToken());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     *
     * @param request
     * @return
     * @throws Exception
     */
    public JwtResponse refreshJwtToken(RefreshTokenRequest request) throws HttpClientErrorException {
        String oldRefreshToken = request.getRefreshToken();

        if (oldRefreshToken.startsWith("Bearer ")) {
            oldRefreshToken = oldRefreshToken.substring(7);
        }

        if (!jwtUtil.validateToken(oldRefreshToken)) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        String username = jwtUtil.extractUsername(oldRefreshToken);
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);


        if(userDetails != null){
            // Optional: Check if token is in DB and hasn't been used
            // If valid, generate new tokens
            String newAccessToken = jwtUtil.generateToken(userDetails);
            String newRefreshToken = jwtUtil.generateToken(userDetails);
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());
            return new JwtResponse(newAccessToken,
                    newRefreshToken,
                    userDetails.getId(),
                    userDetails.getFullName(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles);
        }
        return null;
    }
}