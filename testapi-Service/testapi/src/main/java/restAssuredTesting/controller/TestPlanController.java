package restAssuredTesting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import restAssuredTesting.model.TestPlan;
import restAssuredTesting.model.TestPlanResult;
import restAssuredTesting.service.TestPlanService;
import restAssuredTesting.service.TestRunnerService;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/testplans")
public class TestPlanController {

    private final TestPlanService planService;
    private final TestRunnerService runnerService;

    public TestPlanController(TestPlanService planService, TestRunnerService runnerService) {
        this.planService = planService;
        this.runnerService = runnerService;
    }

    // -------------------------------------------------------------------------
    // 🔹 Retrieve all test plans
    // -------------------------------------------------------------------------
    @GetMapping
    public List<TestPlan> getAll() {
        return planService.findAll();
    }

    // -------------------------------------------------------------------------
    // 🔹 Retrieve a specific test plan by ID
    // -------------------------------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<TestPlan> getById(@PathVariable Long id) {
        return planService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // -------------------------------------------------------------------------
    // 🔹 Create a new test plan
    // -------------------------------------------------------------------------
    @PostMapping
    public TestPlan create(@RequestBody TestPlan plan) {
        return planService.save(plan);
    }

    // -------------------------------------------------------------------------
    // 🔹 Update an existing test plan
    // -------------------------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<TestPlan> update(@PathVariable Long id, @RequestBody TestPlan plan) {
        return ResponseEntity.ok(planService.update(id, plan));
    }

    // -------------------------------------------------------------------------
    // 🔹 Delete a test plan by ID
    // -------------------------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        planService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // 🔹 NEW: Run a specific test plan (execute all scenarios + test cases)
    // -------------------------------------------------------------------------
    @PostMapping("/{id}/run")
    public ResponseEntity<TestPlanResult> runPlan(@PathVariable Long id) {
        return planService.findById(id)
                .map(plan -> {
                    TestPlanResult result = runnerService.runTestPlan(plan);
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
