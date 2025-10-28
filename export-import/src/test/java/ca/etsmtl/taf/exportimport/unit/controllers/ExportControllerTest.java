package ca.etsmtl.taf.exportimport.unit.controllers;

import ca.etsmtl.taf.exportimport.controllers.ExportController;
import ca.etsmtl.taf.exportimport.dtos.ExportRequest;
import ca.etsmtl.taf.exportimport.models.EntityType;
import ca.etsmtl.taf.exportimport.services.ExportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportControllerTest {

    @Mock
    private ExportService exportService;

    @InjectMocks
    private ExportController exportController;

    @Test
    void testExport_Success() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setType("testrail");
        request.getIds().put(EntityType.PROJECT, List.of("123", "456"));

        when(exportService.exportTo(eq("testrail"), any())).thenReturn("Export success");

        ResponseEntity<Map<String, Object>> response = exportController.exportTo(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Export success", response.getBody().get("message"));
    }

    @Test
    void testExport_ServiceThrowsException() throws Exception {
        ExportRequest request = new ExportRequest();
        request.setType("testrail");

        when(exportService.exportTo(eq("testrail"), any()))
                .thenThrow(new RuntimeException("Export failed"));

        ResponseEntity<Map<String, Object>> response = exportController.exportTo(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Export failed", response.getBody().get("message"));
    }
}

