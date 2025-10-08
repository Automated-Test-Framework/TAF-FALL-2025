package ca.etsmtl.taf.exportimport.controllers;

import ca.etsmtl.taf.exportimport.dtos.ExportRequest;
import ca.etsmtl.taf.exportimport.services.ExportService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/export")
public class ExportController {

    private final ExportService exportService;

    @Autowired
    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @PostMapping()
    public ResponseEntity<Map<String, Object>> exportTo(@RequestBody ExportRequest exportRequest) {
        try {
            String message = exportService.exportTo(exportRequest.getType(), exportRequest.getIds());
            return ResponseEntity.ok(Map.of(
                    "message", message
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "message", e.getMessage()
            ));
        }
    }
}