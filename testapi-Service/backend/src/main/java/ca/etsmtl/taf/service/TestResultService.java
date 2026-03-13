package ca.etsmtl.taf.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ca.etsmtl.taf.controller.TestResultController.TestCaseInput;
import ca.etsmtl.taf.entity.TestCaseResult;
import ca.etsmtl.taf.entity.TestRun;
import ca.etsmtl.taf.repository.TestCaseResultRepository;
import ca.etsmtl.taf.repository.TestRunRepository;

@Service
public class TestResultService {

    private static final String PROJECT_KEY = "TAF";

    private final TestRunRepository testRunRepository;
    private final TestCaseResultRepository testCaseResultRepository;

    public TestResultService(TestRunRepository testRunRepository,
                             TestCaseResultRepository testCaseResultRepository) {
        this.testRunRepository = testRunRepository;
        this.testCaseResultRepository = testCaseResultRepository;
    }

    public TestRun saveResults(List<TestCaseInput> cases, String username) {
        String runId = "RUN-" + UUID.randomUUID().toString().substring(0, 8);
        Date now = new Date();

        int total = cases.size();
        int passed = 0;
        int failed = 0;
        long totalDuration = 0;

        List<TestCaseResult> caseResults = new ArrayList<>();

        for (TestCaseInput c : cases) {
            boolean isPassed = c.answer != null && c.answer;
            if (isPassed) passed++; else failed++;

            long duration = c.actualResponseTime != null ? c.actualResponseTime : 0;
            totalDuration += duration;

            TestCaseResult tcr = new TestCaseResult();
            tcr.setProject(PROJECT_KEY);
            tcr.setRunId(runId);
            tcr.setName(buildTestName(c));
            tcr.setSuite("API Tests");
            tcr.setType("API");
            tcr.setTool("RestAssured");
            tcr.setStatus(isPassed ? "passed" : "failed");
            tcr.setDurationMs(duration);
            tcr.setExecutedAt(now);
            tcr.setExecutedBy(username);
            tcr.setRequirements(Collections.emptyList());

            caseResults.add(tcr);
        }

        testCaseResultRepository.saveAll(caseResults);

        TestRun run = new TestRun();

        TestRun.Project project = new TestRun.Project();
        project.setKey(PROJECT_KEY);
        run.setProject(project);

        TestRun.Pipeline pipeline = new TestRun.Pipeline();
        pipeline.setRunId(runId);
        run.setPipeline(pipeline);

        TestRun.Stats stats = new TestRun.Stats();
        stats.setTotal(total);
        stats.setPassed(passed);
        stats.setFailed(failed);
        stats.setDurationMs(totalDuration);

        TestRun.Run runData = new TestRun.Run();
        runData.setStatus(failed > 0 ? "failed" : "passed");
        runData.setStats(stats);
        run.setRun(runData);

        run.setCreatedAt(now);

        testRunRepository.save(run);

        return run;
    }

    private String buildTestName(TestCaseInput c) {
        String method = c.method != null ? c.method : "UNKNOWN";
        String url = c.apiUrl != null ? c.apiUrl : "";
        return method + " " + url;
    }
}
