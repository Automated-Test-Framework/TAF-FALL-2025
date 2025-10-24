package ca.etsmtl.taf.auth.cache;

import ca.etsmtl.taf.auth.jwt.JwtUtil;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TokenCacheTest {

    @Test
    void update() {
        TokenCache cache = new TokenCache();
        String username = "alice";

        JwtResponse jwtResponse = mock(JwtResponse.class);
        when(jwtResponse.getToken()).thenReturn("access-token");
        when(jwtResponse.getRefresh()).thenReturn("refresh-token");

        JwtUtil jwtUtil = mock(JwtUtil.class);
        Date expiry = new Date(System.currentTimeMillis() + 5 * 60 * 1000); // 5 min
        when(jwtUtil.extractExpiration("access-token")).thenReturn(expiry);

        cache.update(username, jwtResponse, jwtUtil);

        Optional<TokenCache.TokenEntry> entryOpt = cache.get(username);
        assertTrue(entryOpt.isPresent(), "entry should be present after update");

        TokenCache.TokenEntry entry = entryOpt.get();
        assertEquals("access-token", entry.getAccess());
        assertEquals("refresh-token", entry.getRefresh());
        assertEquals(expiry, entry.getExpiry());
    }

    @Test
    void get() {
        TokenCache cache = new TokenCache();
        String username = "bob";

        JwtResponse jwtResponse = mock(JwtResponse.class);
        when(jwtResponse.getToken()).thenReturn("tok-bob");
        when(jwtResponse.getRefresh()).thenReturn("ref-bob");

        JwtUtil jwtUtil = mock(JwtUtil.class);
        Date expiry = new Date(System.currentTimeMillis() + 10 * 60 * 1000);
        when(jwtUtil.extractExpiration("tok-bob")).thenReturn(expiry);

        cache.update(username, jwtResponse, jwtUtil);

        Optional<TokenCache.TokenEntry> entryOpt = cache.get(username);
        assertTrue(entryOpt.isPresent());
        TokenCache.TokenEntry entry = entryOpt.get();
        assertEquals("tok-bob", entry.getAccess());
        assertEquals("ref-bob", entry.getRefresh());
    }

    @Test
    void isExpiringSoon() {
        TokenCache cache = new TokenCache();
        String userFar = "charlie";
        String userSoon = "dave";

        JwtResponse respFar = mock(JwtResponse.class);
        when(respFar.getToken()).thenReturn("far-token");
        when(respFar.getRefresh()).thenReturn("far-refresh");

        JwtResponse respSoon = mock(JwtResponse.class);
        when(respSoon.getToken()).thenReturn("soon-token");
        when(respSoon.getRefresh()).thenReturn("soon-refresh");

        JwtUtil jwtUtil = mock(JwtUtil.class);
        Date farExpiry = new Date(System.currentTimeMillis() + 10 * 60 * 1000); // 10 min
        Date soonExpiry = new Date(System.currentTimeMillis() + 30 * 1000); // 30 sec (within 2 min buffer)

        when(jwtUtil.extractExpiration("far-token")).thenReturn(farExpiry);
        when(jwtUtil.extractExpiration("soon-token")).thenReturn(soonExpiry);

        cache.update(userFar, respFar, jwtUtil);
        cache.update(userSoon, respSoon, jwtUtil);

        assertFalse(cache.isExpiringSoon(userFar), "far expiry should not be considered expiring soon");
        assertTrue(cache.isExpiringSoon(userSoon), "soon expiry should be considered expiring soon");

        // unknown user returns false
        assertFalse(cache.isExpiringSoon("nonexistent"));
    }

    @Test
    void remove() {
        TokenCache cache = new TokenCache();
        String username = "eve";

        JwtResponse jwtResponse = mock(JwtResponse.class);
        when(jwtResponse.getToken()).thenReturn("tok-eve");
        when(jwtResponse.getRefresh()).thenReturn("ref-eve");

        JwtUtil jwtUtil = mock(JwtUtil.class);
        when(jwtUtil.extractExpiration("tok-eve")).thenReturn(new Date(System.currentTimeMillis() + 60_000));

        cache.update(username, jwtResponse, jwtUtil);
        assertTrue(cache.get(username).isPresent());

        cache.remove(username);
        assertFalse(cache.get(username).isPresent());
    }

    @Test
    void getCache() {
        TokenCache cache = new TokenCache();
        String username = "frank";

        JwtResponse jwtResponse = mock(JwtResponse.class);
        when(jwtResponse.getToken()).thenReturn("tok-frank");
        when(jwtResponse.getRefresh()).thenReturn("ref-frank");

        JwtUtil jwtUtil = mock(JwtUtil.class);
        Date expiry = new Date(System.currentTimeMillis() + 2 * 60 * 1000);
        when(jwtUtil.extractExpiration("tok-frank")).thenReturn(expiry);

        cache.update(username, jwtResponse, jwtUtil);

        Map<String, TokenCache.TokenEntry> map = cache.getCache();
        assertNotNull(map);
        assertTrue(map.containsKey(username));
        TokenCache.TokenEntry entry = map.get(username);
        assertEquals("tok-frank", entry.getAccess());
        assertEquals("ref-frank", entry.getRefresh());
        assertEquals(expiry, entry.getExpiry());
    }
}