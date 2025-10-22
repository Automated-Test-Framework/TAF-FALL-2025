package ca.etsmtl.taf.auth.config;

import ca.etsmtl.taf.auth.services.BasicAuthStrategy;
import ca.etsmtl.taf.auth.services.JwtAuthStrategy;
import ca.etsmtl.taf.auth.services.OAuthAuthStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthStrategyConfigTest {

    @Mock
    private JwtAuthStrategy jwtMock;

    @Mock
    private OAuthAuthStrategy oauthMock;

    @Mock
    private BasicAuthStrategy basicMock;

    @InjectMocks
    private AuthStrategyConfig cfg;


    @Test
    public void authStrategy() throws Exception {
        Field f = AuthStrategyConfig.class.getDeclaredField("authMode");
        f.setAccessible(true);

        f.set(cfg, "jwt");
        assertSame(jwtMock, cfg.authStrategy());

        f.set(cfg, "basic");
        assertSame(basicMock, cfg.authStrategy());

        f.set(cfg, "oauth");
        assertSame(oauthMock, cfg.authStrategy());

        assertThrows(IllegalStateException.class, () -> {
            f.set(cfg, "invalid");
            cfg.authStrategy();
        });
    }
}