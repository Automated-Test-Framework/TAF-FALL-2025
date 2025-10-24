package restAssuredTesting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestScenarioResult {
    private String scenarioName;
    private int totalCases;
    private int passed;
    private int failed;
    private List<TestResult> caseResults;
}