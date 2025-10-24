package restAssuredTesting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import restAssuredTesting.model.TestCase;
import restAssuredTesting.service.TestCaseService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/testcases")
public class TestCaseController {

    private final TestCaseService service;

    public TestCaseController(TestCaseService service) {
        this.service = service;
    }

    @GetMapping
    public List<TestCase> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestCase> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TestCase create(@RequestBody TestCase testCase) {
        return service.save(testCase);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestCase> update(@PathVariable Long id,
                                           @RequestBody TestCase testCase) {
        return ResponseEntity.ok(service.update(id, testCase));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
