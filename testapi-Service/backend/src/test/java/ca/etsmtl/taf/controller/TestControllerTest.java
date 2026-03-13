package ca.etsmtl.taf.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import ca.etsmtl.taf.security.services.UserDetailsImpl;

@WebMvcTest(TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TestController — /api/test role-based access")
class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ==================== /all (public) ====================

    @Test
    @DisplayName("GET /api/test/all — accessible without authentication")
    void allAccess_noAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/test/all"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bienvenue au TAF."));
    }

    // ==================== /user (ROLE_USER or ROLE_ADMIN) ====================

    @Test
    @DisplayName("GET /api/test/user — returns User Content")
    void userAccess_returns200() throws Exception {
        mockMvc.perform(get("/api/test/user"))
                .andExpect(status().isOk())
                .andExpect(content().string("User Content."));
    }

    // ==================== /admin (ROLE_ADMIN only) ====================

    @Test
    @DisplayName("GET /api/test/admin — returns Admin Board")
    void adminAccess_returns200() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin Board."));
    }
}
