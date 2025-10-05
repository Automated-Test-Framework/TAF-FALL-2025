package ca.etsmtl.taf.exportimport.integration.controllers;

import ca.etsmtl.taf.exportimport.services.ImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ImportService importService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testExport_Success() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "type", "testrail",
                "project", List.of("p1", "p2"),
                "suite", List.of("s1"),
                "case", List.of("c1"),
                "run", List.of("r1")
        );

        when(importService.importTo(eq("testrail"), any())).thenReturn("Import success");

        mockMvc.perform(post("/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Import success"));
    }

    @Test
    void testExport_failed() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "type", "testrail",
                "project", List.of("p1", "p2")
        );

        when(importService.importTo(eq("testrail"), any())).thenThrow(new Exception("Import failed"));

        mockMvc.perform(post("/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("Import failed"));
    }
}
