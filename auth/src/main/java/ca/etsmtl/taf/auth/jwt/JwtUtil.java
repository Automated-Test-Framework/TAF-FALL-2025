package ca.etsmtl.taf.auth.jwt;

import ca.etsmtl.taf.auth.model.enums.TokenClaims;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${taf.app.jwtSecret}")
    private String secret;

    @Value("${taf.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    /* ==========================
     *     TOKEN EXTRACTION
     * ========================== */
    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject(); // still get subject even if expired
        } catch (Exception e) {
            log.warn("Failed to extract username: {}", e.getMessage());
            return null;
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (ExpiredJwtException e) {
            return e.getClaims().getExpiration();
        } catch (Exception e) {
            log.warn("Failed to extract expiration: {}", e.getMessage());
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // Return claims even if expired (used for refresh)
            return e.getClaims();
        }
    }

    /* ==========================
     *     VALIDATION
     * ========================== */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username != null && username.equals(userDetails.getUsername()) && !isExpired(token));
    }

    public Boolean validateToken(String token) {
        try {
            return !isExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean isExpired(String token) {
        try {
            Date exp = extractExpiration(token);
            return exp != null && exp.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isExpiringSoon(String token, long bufferMs) {
        try {
            Date exp = extractExpiration(token);
            if (exp == null) return false;
            long remaining = exp.getTime() - System.currentTimeMillis();
            return remaining > 0 && remaining < bufferMs;
        } catch (Exception e) {
            log.warn("Failed to check token expiry: {}", e.getMessage());
            return false;
        }
    }

    /* ==========================
     *     GENERATION
     * ========================== */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String refreshToken(String token) {
        String jwt = token;
        if (token.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        String username = extractUsername(jwt);
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }
}
