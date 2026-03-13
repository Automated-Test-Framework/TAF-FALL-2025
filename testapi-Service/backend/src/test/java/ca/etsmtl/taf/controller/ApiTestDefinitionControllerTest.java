package ca.etsmtl.taf.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.etsmtl.taf.entity.ApiTestDefinition;
import ca.etsmtl.taf.repository.ApiTestDefinitionRepository;
import ca.etsmtl.taf.security.WebSecurityConfig;
import ca.etsmtl.taf.security.jwt.AuthEntryPointJwt;
import ca.etsmtl.taf.security.jwt.JwtUtils;
import ca.etsmtl.taf.security.oauth2.OAuth2LoginSuccessHandler;
import ca.etsmtl.taf.security.services.UserDetailsServiceImpl;

@WebMvcTest(controllers = ApiTestDefinitionController.class)
@Import({WebSecurityConfig.class, AuthEntryPointJwt.class})
@DisplayName("ApiTestDefinitionController — CRUD /api/testapi/definitions")
class ApiTestDefinitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApiTestDefinitionRepository repository;

    // Security dependencies
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @MockitoBean
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    @MockitoBean
    private JwtUtils jwtUtils;

    // ── helpers ──────────────────────────────────────

    private ApiTestDefinition makeDef(String id, String username) {
        ApiTestDefinition d = new ApiTestDefinition();
        d.setId(id);
        d.setUsername(username);
        d.setMethod("GET");
        d.setApiUrl("https://api.example.com/items");
        d.setStatusCode(200);
        d.setHeaders(Map.of("Accept", "application/json"));
        d.setCreatedAt(new Date());
        return d;
    }

    // ── GET /api/testapi/definitions ─────────────────

    @Test
    @WithMockUser(username = "equipe3")
    @DisplayName("GET returns definitions for the authenticated user")
    void getAll_returnsUserDefinitions() throws Exception {
        List<ApiTestDefinition> defs = List.of(makeDef("abc1", "equipe3"), makeDef("abc2", "equipe3"));
        when(repository.findByUsernameOrderByCreatedAtAsc("equipe3")).thenReturn(defs);

        mockMvc.perform(get("/api/testapi/definitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].method").value("GET"))
                .andExpect(jsonPath("$[0].apiUrl").value("https://api.example.com/items"));
    }

    @Test
    @DisplayName("GET without auth returns 401")
    void getAll_noAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/testapi/definitions"))
                .andExpect(status().isUnauthorized());
    }

    // ── POST /api/testapi/definitions ────────────────

    @Test
    @WithMockUser(username = "equipe3")
    @DisplayName("POST creates a new definition for the authenticated user")
    void create_savesDefinition() throws Exception {
        ApiTestDefinition input = makeDef(null, null);
        ApiTestDefinition saved = makeDef("newId1", "equipe3");

        when(repository.save(any(ApiTestDefinition.class))).thenReturn(saved);

        mockMvc.perform(post("/api/testapi/definitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("newId1"))
                .andExpect(jsonPath("$.username").value("equipe3"));

        verify(repository).save(argThat(def ->
                "equipe3".equals(def.getUsername()) && def.getId() == null));
    }

    // ── PUT /api/testapi/definitions/{id} ────────────

    @Test
    @WithMockUser(username = "equipe3")
    @DisplayName("PUT updates an existing definition owned by user")
    void update_ownDefinition_succeeds() throws Exception {
        ApiTestDefinition existing = makeDef("abc1", "equipe3");
        ApiTestDefinition updated = makeDef("abc1", "equipe3");
        updated.setMethod("POST");

        when(repository.findById("abc1")).thenReturn(Optional.of(existing));
        when(repository.save(any(ApiTestDefinition.class))).thenReturn(updated);

        ApiTestDefinition body = makeDef(null, null);
        body.setMethod("POST");

        mockMvc.perform(put("/api/testapi/definitions/abc1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.method").value("POST"));
    }

    @Test
    @WithMockUser(username = "equipe3")
    @DisplayName("PUT on another user's definition returns 403")
    void update_otherUserDefinition_returns403() throws Exception {
        ApiTestDefinition existing = makeDef("abc1", "otheruser");
        when(repository.findById("abc1")).thenReturn(Optional.of(existing));

        mockMvc.perform(put("/api/testapi/definitions/abc1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeDef(null, null))))
                .andExpect(status().isForbidden());

        verify(repository, never()).save(any());
    }

    @Test
    @WithMockUser(username = "equipe3")
    @DisplayName("PUT on non-existent id returns 404")
    void update_nonExistentId_returns404() throws Exception {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/testapi/definitions/missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeDef(null, null))))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /api/testapi/definitions/{id} ─────────

    @Test
    @WithMockUser(username = "equipe3")
    @DisplayName("DELETE removes an existing definition owned by user")
    void delete_ownDefinition_succeeds() throws Exception {
        ApiTestDefinition existing = makeDef("abc1", "equipe3");
        when(repository.findById("abc1")).thenReturn(Optional.of(existing));

        mockMvc.perform(delete("/api/testapi/definitions/abc1"))
                .andExpect(status().isNoContent());

        verify(repository).deleteById("abc1");
    }

    @Test
    @WithMockUser(username = "equipe3")
    @DisplayName("DELETE on another user's definition returns 403")
    void delete_otherUserDefinition_returns403() throws Exception {
        ApiTestDefinition existing = makeDef("abc1", "otheruser");
        when(repository.findById("abc1")).thenReturn(Optional.of(existing));

        mockMvc.perform(delete("/api/testapi/definitions/abc1"))
                .andExpect(status().isForbidden());

        verify(repository, never()).deleteById(any());
    }

    // ── Entity ───────────────────────────────────────

    @Test
    @DisplayName("ApiTestDefinition getters/setters work correctly")
    void entity_gettersSetters() {
        ApiTestDefinition d = new ApiTestDefinition();
        d.setId("id1");
        d.setUsername("user1");
        d.setMethod("DELETE");
        d.setApiUrl("https://api.test.com");
        d.setStatusCode(204);
        d.setResponseTime(3000);
        d.setInput("{\"k\":\"v\"}");
        d.setExpectedOutput("{}");
        d.setHeaders(Map.of("X-Key", "val"));
        d.setExpectedHeaders(Map.of("Content-Type", "text/plain"));
        Date now = new Date();
        d.setCreatedAt(now);
        d.setUpdatedAt(now);

        assertThat(d.getId(), is("id1"));
        assertThat(d.getUsername(), is("user1"));
        assertThat(d.getMethod(), is("DELETE"));
        assertThat(d.getApiUrl(), is("https://api.test.com"));
        assertThat(d.getStatusCode(), is(204));
        assertThat(d.getResponseTime(), is(3000));
        assertThat(d.getInput(), is("{\"k\":\"v\"}"));
        assertThat(d.getExpectedOutput(), is("{}"));
        assertThat(d.getHeaders(), hasEntry("X-Key", "val"));
        assertThat(d.getExpectedHeaders(), hasEntry("Content-Type", "text/plain"));
        assertThat(d.getCreatedAt(), is(now));
        assertThat(d.getUpdatedAt(), is(now));
    }
}
