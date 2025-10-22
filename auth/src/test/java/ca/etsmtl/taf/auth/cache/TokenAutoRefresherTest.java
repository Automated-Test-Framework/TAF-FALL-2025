package ca.etsmtl.taf.auth.cache;

import ca.etsmtl.taf.auth.cache.TokenCache.TokenEntry;
import ca.etsmtl.taf.auth.jwt.JwtUtil;
import ca.etsmtl.taf.auth.payload.request.RefreshTokenRequest;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import ca.etsmtl.taf.auth.services.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenAutoRefresherTest {

    @Mock
    private TokenCache tokenCache;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private ca.etsmtl.taf.auth.scheduler.TokenAutoRefresher refresher;

    @Test
    void refreshExpiringTokens_refreshesSuccessfully() {
        TokenEntry entry = mock(TokenEntry.class);
        when(entry.getAccess()).thenReturn("access1");
        when(entry.getRefresh()).thenReturn("refresh1");

        Map<String, TokenEntry> map = new HashMap<>();
        map.put("user1", entry);

        when(tokenCache.getCache()).thenReturn(map);
        when(jwtUtil.isExpiringSoon("access1", 120000)).thenReturn(true);

        JwtResponse newTokens = mock(JwtResponse.class);
        when(newTokens.getToken()).thenReturn("newAccess");
        when(jwtService.refreshJwtToken(any(RefreshTokenRequest.class))).thenReturn(newTokens);

        refresher.refreshExpiringTokens();

        verify(jwtService).refreshJwtToken(any(RefreshTokenRequest.class));
        verify(tokenCache).update("user1", newTokens, jwtUtil);
    }

    @Test
    void refreshExpiringTokens_notExpiring_noRefresh() {
        TokenEntry entry = mock(TokenEntry.class);
        when(entry.getAccess()).thenReturn("access2");
        when(entry.getRefresh()).thenReturn("refresh2");

        Map<String, TokenEntry> map = new HashMap<>();
        map.put("user2", entry);

        when(tokenCache.getCache()).thenReturn(map);
        when(jwtUtil.isExpiringSoon("access2", 120000)).thenReturn(false);

        refresher.refreshExpiringTokens();

        verify(jwtService, never()).refreshJwtToken(any());
        verify(tokenCache, never()).update(anyString(), any(), any());
    }

    @Test
    void refreshExpiringTokens_missingTokens_skipsEntry() {
        TokenEntry entry = mock(TokenEntry.class);
        lenient().when(entry.getAccess()).thenReturn(null);
        lenient().when(entry.getRefresh()).thenReturn(null);


        Map<String, TokenEntry> map = new HashMap<>();
        map.put("user3", entry);

        when(tokenCache.getCache()).thenReturn(map);

        refresher.refreshExpiringTokens();

        verify(jwtService, never()).refreshJwtToken(any());
        verify(tokenCache, never()).update(anyString(), any(), any());
    }

    @Test
    void refreshExpiringTokens_refreshThrows_noUpdate() {
        TokenEntry entry = mock(TokenEntry.class);
        when(entry.getAccess()).thenReturn("access4");
        when(entry.getRefresh()).thenReturn("refresh4");

        Map<String, TokenEntry> map = new HashMap<>();
        map.put("user4", entry);

        when(tokenCache.getCache()).thenReturn(map);
        when(jwtUtil.isExpiringSoon("access4", 120000)).thenReturn(true);

        when(jwtService.refreshJwtToken(any())).thenThrow(new RuntimeException("refresh failed"));

        refresher.refreshExpiringTokens();

        verify(tokenCache, never()).update(anyString(), any(), any());
    }
}