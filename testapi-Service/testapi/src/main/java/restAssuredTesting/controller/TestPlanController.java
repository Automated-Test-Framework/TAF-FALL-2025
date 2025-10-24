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

    @GetMapping
    public List<TestPlan> getAll() {
        return planService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TestPlan> getById(@PathVariable Long id) {
        return planService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public TestPlan create(@RequestBody TestPlan plan) {
        return planService.save(plan);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TestPlan> update(@PathVariable Long id, @RequestBody TestPlan plan) {
        return ResponseEntity.ok(planService.update(id, plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        planService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

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
