package ca.etsmtl.taf.exportimport.controllers;

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
                    "success", true,
                    "data", caseData
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}

