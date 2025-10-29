package ca.etsmtl.taf.auth.config;


import ca.etsmtl.taf.auth.jwt.JwtAuthenticationFilter;
import ca.etsmtl.taf.auth.jwt.JwtUtil;
import ca.etsmtl.taf.auth.model.CustomUserDetails;
import ca.etsmtl.taf.auth.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import ca.etsmtl.taf.auth.handlers.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequestWrapper;


@Configuration
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    CustomUserDetailsService userDetailsService;

    @Autowired
    CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CookieToBearerFilter cookieToBearerFilter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/auth/**",
                                "/api/signin",
                                "/api/signup",
                                "/api/me",
                                "/api/refresh-token",
                                "/api/validate-token",
                                "/api/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs",
                                "/api-docs/**",
                                "/webjars/**",
                                "/webjars/swagger-ui/index.html",
                                "/example/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                /*.authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )*/
                .oauth2Login(oauth -> oauth
                        .loginPage("/oauth2/authorization/google")
                        .successHandler(oauthSuccessHandler()) // ADD
                )
                .addFilterBefore(cookieToBearerFilter, UsernamePasswordAuthenticationFilter.class)

                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    // ADD
    @Bean
    AuthenticationSuccessHandler oauthSuccessHandler() {
        return (request, response, authentication) -> {
            var oauth = (OAuth2AuthenticationToken) authentication;
            var principal = (DefaultOidcUser) oauth.getPrincipal(); // scopes: openid,email,profile
            String email = principal.getEmail();
            // réutilise TON service JWT existant
            String access = jwtUtil.generateTokenWithEmail(email);//"";//jwtService.issueAccessToken(email);     // même durée que ton /auth/login
            String refresh = jwtUtil.generateTokenWithEmail(email);//;//jwtService.issueRefreshToken(email);   // idem

            addHttpOnlyCookie(response, "APP_JWT", access,   15 * 60);        // aligne avec ta conf
            addHttpOnlyCookie(response, "APP_REFRESH", refresh, 7 * 24 * 3600);

            response.sendRedirect("http://localhost:4200/"); // adapte si besoin
        };
    }

    // ADD: petit helper cookie
    private static void addHttpOnlyCookie(HttpServletResponse res, String name, String value, int maxAgeSec) {
        Cookie c = new Cookie(name, value);
        c.setPath("/");
        c.setMaxAge(maxAgeSec);
        c.setHttpOnly(true);
        // en prod: c.setSecure(true); c.setAttribute("SameSite","None");
        res.addCookie(c);
    }

}