package ca.etsmtl.taf.auth.config;

import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Component
public class CookieToBearerFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        if (req.getHeader("Authorization") == null && req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("APP_JWT".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                    HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(req) {
                        @Override public String getHeader(String name) {
                            if ("Authorization".equalsIgnoreCase(name)) return "Bearer " + c.getValue();
                            return super.getHeader(name);
                        }
                        @Override public Enumeration<String> getHeaders(String name) {
                            if ("Authorization".equalsIgnoreCase(name)) {
                                return Collections.enumeration(List.of("Bearer " + c.getValue()));
                            }
                            return super.getHeaders(name);
                        }
                    };
                    chain.doFilter(wrapper, res);
                    return;
                }
            }
        }
        chain.doFilter(req, res);
    }

    /*@Bean
    CorsConfigurationSource corsConfigurationSource() {
        var cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("http://localhost:4200"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        var src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }*/
}