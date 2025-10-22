package ca.etsmtl.taf.auth.scheduler;

import ca.etsmtl.taf.auth.cache.TokenCache;
import ca.etsmtl.taf.auth.cache.TokenCache.TokenEntry;
import ca.etsmtl.taf.auth.jwt.JwtUtil;
import ca.etsmtl.taf.auth.payload.request.RefreshTokenRequest;
import ca.etsmtl.taf.auth.payload.response.JwtResponse;
import ca.etsmtl.taf.auth.services.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class TokenAutoRefresher {

    private final TokenCache tokenCache;
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;

    // Runs every 60 seconds
    @Scheduled(fixedDelay = 60000)
    public void refreshExpiringTokens() {
        try {
            for (Map.Entry<String, TokenEntry> entry : tokenCache.getCache().entrySet()) {
                String username = entry.getKey();
                TokenEntry tokenEntry = entry.getValue();

                if (tokenEntry == null || tokenEntry.getAccess() == null || tokenEntry.getRefresh() == null) continue;

                if (jwtUtil.isExpiringSoon(tokenEntry.getAccess(), 120000)) {
                    log.info("Refreshing token for '{}'", username);

                    try {
                        RefreshTokenRequest request = new RefreshTokenRequest();
                        request.setRefreshToken(tokenEntry.getRefresh());

                        JwtResponse newTokens = jwtService.refreshJwtToken(request);

                        if (newTokens != null && newTokens.getToken() != null) {
                            tokenCache.update(username, newTokens, jwtUtil);
                            log.info("Token refreshed successfully for '{}'", username);
                        }
                    } catch (Exception ex) {
                        log.error("Failed to refresh token for '{}': {}", username, ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error in TokenAutoRefresher: {}", e.getMessage());
        }
    }

}
