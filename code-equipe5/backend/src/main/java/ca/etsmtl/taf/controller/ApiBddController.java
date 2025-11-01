package ca.etsmtl.taf.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import ca.etsmtl.taf.service.ApiBddService;

@Profile("!local")
@RestController
@RequestMapping("/api/results")
@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE}, allowCredentials = "true")
public class ApiBddController {

    @Autowired
    private ApiBddService apiBddService;

    // POST payload given a specific module
    @PostMapping("/{module}")
    public ResponseEntity<String> saveResults(
            @PathVariable String module,
            @RequestBody JsonNode resultData) {
        try {
            apiBddService.saveResult(module, resultData);
            return ResponseEntity.status(HttpStatus.CREATED).body("Résultats sauvegardés avec succès pour le module: " + module);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la sauvegarde des résultats.");
        }
    }

    // GET all results for a specific module
    @GetMapping("/{module}")
    public ResponseEntity<?> getAllResults(@PathVariable String module) {
        return ResponseEntity.ok(apiBddService.getAllResultsByModule(module));
    }

    // DELETE all results for a specific module
    @DeleteMapping("/{module}")
    public ResponseEntity<?> deleteAllResults(@PathVariable String module) {
        apiBddService.deleteAllResultsByModule(module);
        return ResponseEntity.noContent().build();
    }

    // GET a specific result by ID for a module
    @GetMapping("/{module}/{id}")
    public ResponseEntity<?> getResultById(@PathVariable String module, @PathVariable Long id) {
        return ResponseEntity.ok(apiBddService.getResultById(module, id));
    }

    // DELETE a specific result by ID for a module
    @DeleteMapping("/{module}/{id}")
    public ResponseEntity<?> deleteResultById(@PathVariable String module, @PathVariable Long id) {
        apiBddService.deleteResultById(module, id);
        return ResponseEntity.noContent().build();
    }
}
