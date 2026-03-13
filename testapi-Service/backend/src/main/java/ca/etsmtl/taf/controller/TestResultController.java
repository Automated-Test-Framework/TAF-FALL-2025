package ca.etsmtl.taf.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import ca.etsmtl.taf.entity.TestRun;
import ca.etsmtl.taf.service.TestResultService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/testapi/results")
@SecurityRequirement(name = "bearerAuth")
public class TestResultController {

    private final TestResultService testResultService;

    public TestResultController(TestResultService testResultService) {
        this.testResultService = testResultService;
    }

    @PostMapping
    public ResponseEntity<TestRun> saveResults(@RequestBody List<TestCaseInput> cases,
                                                Authentication auth) {
        TestRun run = testResultService.saveResults(cases, auth.getName());
        return ResponseEntity.ok(run);
    }

    /**
     * DTO matching the frontend payload (subset of testModel2 + result fields).
     */
    public static class TestCaseInput {
        public String method;
        public String apiUrl;
        public Integer statusCode;
        public Boolean answer;         // true = passed
        public Long actualResponseTime;
    }
}
