package ca.etsmtl.taf.exportimport.controllers;

import ca.etsmtl.taf.exportimport.dtos.ImportRequest;
import ca.etsmtl.taf.exportimport.services.ImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/import")
public class ImportController {

    private final ImportService importService;

    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping()
    public ResponseEntity<Map<String, Object>> importTo(@RequestBody ImportRequest importRequest) {
        try {
            String message = importService.importTo(importRequest.getType(), importRequest.getIds());
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

