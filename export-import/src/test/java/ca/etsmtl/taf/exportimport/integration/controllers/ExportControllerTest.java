package ca.etsmtl.taf.exportimport.integration.controllers;

import ca.etsmtl.taf.exportimport.services.ExportService;
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
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExportService exportService;

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

        when(exportService.exportTo(eq("testrail"), any())).thenReturn("Export success");

        mockMvc.perform(post("/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Export success"));
    }

    @Test
    void testExport_failed() throws Exception {
        Map<String, Object> requestBody = Map.of(
                "type", "testrail",
                "project", List.of("p1", "p2")
        );

        when(exportService.exportTo(eq("testrail"), any())).thenThrow(new Exception("Export failed"));

        mockMvc.perform(post("/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Export failed"));
    }
}
