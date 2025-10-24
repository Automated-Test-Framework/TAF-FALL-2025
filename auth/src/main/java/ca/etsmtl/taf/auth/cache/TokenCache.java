package ca.etsmtl.taf.auth.cache;

import ca.etsmtl.taf.auth.jwt.JwtUtil;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TokenCache {

    @Getter
    private final Map<String, TokenEntry> cache = new ConcurrentHashMap<>();

    private static final long EXPIRATION_BUFFER_MS = 2 * 60 * 1000; // 2 min

    public void update(String username, JwtResponse jwtResponse, JwtUtil jwtUtil) {
        if (jwtResponse == null || jwtResponse.getToken() == null) return;

        Date expiry = jwtUtil.extractExpiration(jwtResponse.getToken());
        TokenEntry entry = new TokenEntry(jwtResponse.getToken(), jwtResponse.getRefresh(), expiry);

        cache.put(username, entry);
        log.info("TokenCache updated for '{}' (expires {}).", username, expiry);
    }

    public Optional<TokenEntry> get(String username) {
        return Optional.ofNullable(cache.get(username));
    }

    public boolean isExpiringSoon(String username) {
        TokenEntry entry = cache.get(username);
        if (entry == null || entry.getExpiry() == null) return false;
        return entry.getExpiry().getTime() - System.currentTimeMillis() < EXPIRATION_BUFFER_MS;
    }

    public void remove(String username) {
        cache.remove(username);
    }

    @Getter
    public static class TokenEntry {
        private final String access;
        private final String refresh;
        private final Date expiry;

        public TokenEntry(String access, String refresh, Date expiry) {
            this.access = access;
            this.refresh = refresh;
            this.expiry = expiry;
        }
    }
}
