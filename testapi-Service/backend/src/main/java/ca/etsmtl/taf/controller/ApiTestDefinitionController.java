package ca.etsmtl.taf.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import ca.etsmtl.taf.entity.ApiTestDefinition;
import ca.etsmtl.taf.repository.ApiTestDefinitionRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/testapi/definitions")
@SecurityRequirement(name = "bearerAuth")
public class ApiTestDefinitionController {

    private final ApiTestDefinitionRepository repository;

    public ApiTestDefinitionController(ApiTestDefinitionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<ApiTestDefinition>> getAll(Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(repository.findByUsernameOrderByCreatedAtAsc(username));
    }

    @PostMapping
    public ResponseEntity<ApiTestDefinition> create(@RequestBody ApiTestDefinition def, Authentication auth) {
        def.setId(null);
        def.setUsername(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(repository.save(def));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiTestDefinition> update(@PathVariable("id") String id,
                                                     @RequestBody ApiTestDefinition def,
                                                     Authentication auth) {
        String username = auth.getName();
        return repository.findById(id)
                .map(existing -> {
                    if (!username.equals(existing.getUsername())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<ApiTestDefinition>build();
                    }
                    def.setId(id);
                    def.setUsername(username);
                    def.setCreatedAt(existing.getCreatedAt());
                    return ResponseEntity.ok(repository.save(def));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id, Authentication auth) {
        String username = auth.getName();
        return repository.findById(id)
                .map(existing -> {
                    if (!username.equals(existing.getUsername())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<Void>build();
                    }
                    repository.deleteById(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
