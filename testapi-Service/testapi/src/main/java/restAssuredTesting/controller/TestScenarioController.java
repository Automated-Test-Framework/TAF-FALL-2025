package restAssuredTesting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import restAssuredTesting.model.TestScenario;
import restAssuredTesting.service.TestScenarioService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/testscenarios")
public class TestScenarioController {

    private final TestScenarioService service;

    public TestScenarioController(TestScenarioService service) {
        this.service = service;
    }

    @GetMapping
    public List<TestScenario> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestScenario> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TestScenario create(@RequestBody TestScenario scenario) {
        return service.save(scenario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestScenario> update(@PathVariable Long id,
                                               @RequestBody TestScenario scenario) {
        return ResponseEntity.ok(service.update(id, scenario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
