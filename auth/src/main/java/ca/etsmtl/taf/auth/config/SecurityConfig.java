package ca.etsmtl.taf.auth.config;

import ca.etsmtl.taf.auth.jwt.JwtAuthenticationFilter;
import ca.etsmtl.taf.auth.jwt.JwtUtil;
import ca.etsmtl.taf.auth.services.CustomUserDetailsService;
import ca.etsmtl.taf.auth.handlers.CustomAccessDeniedHandler;
import ca.etsmtl.taf.auth.handlers.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${feature.auth.mode:jwt}")
    private String authMode;   // jwt | oauth | basic

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AuthenticationManager authManager) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/auth/**",
                                "/api/signin",
                                "/api/signup",
                                "/api/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs",
                                "/webjars/**",
                                "/example/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        switch (authMode.toLowerCase()) {
            case "basic" -> {
                BasicAuthenticationEntryPoint basicEntryPoint = new BasicAuthenticationEntryPoint();
                basicEntryPoint.setRealmName("tafa-realm");
                basicEntryPoint.afterPropertiesSet();
                http.addFilterBefore(
                        new BasicAuthenticationFilter(authManager, basicEntryPoint),
                        UsernamePasswordAuthenticationFilter.class);
            }
            case "jwt" -> {
                http.addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);
            }
            case "oauth" -> {
                // Simulated OAuth2 mode (no spring-security-oauth2 classes used)
                // All logic handled in OAuthAuthStrategy
                http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
                // Here, you simulate OAuth2 since external client registrations are optional
//                http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
            }
            default -> throw new IllegalStateException(
                    "Invalid authentication mode: " + authMode);
        }

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
