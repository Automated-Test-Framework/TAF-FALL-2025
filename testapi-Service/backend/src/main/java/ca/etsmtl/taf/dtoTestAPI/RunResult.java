package ca.etsmtl.taf.dtoTestAPI;

import java.util.ArrayList;
import java.util.List;

public class RunResult {
    private String suiteName;
    private List<StepResult> tests = new ArrayList<>();

    public RunResult() {}
    public RunResult(String suiteName) { this.suiteName = suiteName; }

    public String getSuiteName() { return suiteName; }
    public void setSuiteName(String suiteName) { this.suiteName = suiteName; }

    public List<StepResult> getTests() { return tests; }
    public void setTests(List<StepResult> tests) { this.tests = tests; }

    public boolean isAllPassed() {
        return tests.stream().allMatch(StepResult::isPassed);
    }
}
