package ca.etsmtl.taf.auth.jwt;

import ca.etsmtl.taf.auth.model.enums.TokenClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${taf.app.jwtSecret}")
    private String secret;

    @Value("${taf.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    public Boolean validateToken(String token) {
        /*SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();*/

        // Optional: check expiration manually
        // return !claims.getExpiration().before(new java.util.Date());
        return true;
    }

    public boolean isExpiringSoon(String token, long bufferMs) {
        Date exp = extractExpiration(token);
        return exp != null && exp.getTime() - System.currentTimeMillis() < bufferMs;
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    private String refreshToken(Map<String, Object> claims, final String token) {
        String jwt = token;
        if (token.startsWith("Bearer ")) {
            jwt = jwt.substring(7);
        }
        String username = this.extractUsername(jwt);
        return Jwts.builder().setClaims(claims).setSubject(username).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }
}