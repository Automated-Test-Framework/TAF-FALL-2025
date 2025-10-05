package ca.etsmtl.taf.exportimport.controllers;


import ca.etsmtl.taf.exportimport.dtos.ExportRequest;
import ca.etsmtl.taf.exportimport.services.ExportService;
import org.json.simple.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/export")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/case/{id}")
    public ResponseEntity<?> getCase(@PathVariable("id") int caseId) {
        try {
            JSONObject caseData = exportService.getTestCase(caseId);
            return ResponseEntity.ok(Map.of(
                    "data", caseData
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "message", e.getMessage()
            ));
        }
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