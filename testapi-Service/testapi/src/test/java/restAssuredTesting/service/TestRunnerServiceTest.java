package restAssuredTesting.service;

import org.junit.jupiter.api.Test;
import restAssuredTesting.model.TestCase;
import restAssuredTesting.model.TestPlan;
import restAssuredTesting.model.TestPlanResult;
import restAssuredTesting.model.TestScenario;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestRunnerServiceTest {

    private final TestRunnerService service = new TestRunnerService();

    @Test
    void runTestPlan_ShouldReturnSummary() {
        TestCase testCase = new TestCase();
        testCase.setMethod("GET");
        testCase.setEndpoint("https://httpbin.org/get");
        testCase.setExpectedStatus(200);

        TestScenario scenario = new TestScenario();
        scenario.setName("SimpleScenario");
        scenario.setTestCases(List.of(testCase));

        TestPlan plan = new TestPlan();
        plan.setName("SimplePlan");
        plan.setScenarios(List.of(scenario));

        TestPlanResult result = service.runTestPlan(plan);
        assertEquals(1, result.getTotalScenarios());
        assertTrue(result.getTotalCases() > 0);
    }
}
