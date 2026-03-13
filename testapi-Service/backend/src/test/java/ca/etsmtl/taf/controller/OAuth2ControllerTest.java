package ca.etsmtl.taf.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OAuth2Controller.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("OAuth2Controller — /api/oauth2 endpoints")
class OAuth2ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/oauth2/login-url returns provider info (public endpoint)")
    void loginUrl_returnsProviderInfo() throws Exception {
        mockMvc.perform(get("/api/oauth2/login-url"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.provider").value("google"))
                .andExpect(jsonPath("$.loginUrl").value("/oauth2/authorization/google"))
                .andExpect(jsonPath("$.description").exists());
    }
}
