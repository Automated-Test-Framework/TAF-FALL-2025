package restAssuredTesting.service;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import org.springframework.stereotype.Service;
import restAssuredTesting.model.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestRunnerService {

    // -------------------------------------------------------------------------
    // 🔹 Global configuration block (executed once)
    // -------------------------------------------------------------------------
    static {
        RestAssured.baseURI = "http://localhost:8080";

        // ✅ Global timeout settings (in milliseconds)
        RestAssured.config = RestAssuredConfig.config().httpClient(
                HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 10000)          // 10s to connect
                        .setParam("http.socket.timeout", 10000)              // 10s to wait for response
                        .setParam("http.connection-manager.timeout", 10000)  // 10s for connection pool
        );
    }

    // -------------------------------------------------------------------------
    // 🔹 Run a full test plan (scenarios + cases)
    // -------------------------------------------------------------------------
    public TestPlanResult runTestPlan(TestPlan plan) {
        List<TestScenarioResult> scenarioResults = new ArrayList<>();

        int totalCases = 0;
        int totalPassed = 0;
        int totalFailed = 0;

        for (TestScenario scenario : plan.getScenarios()) {
            List<TestResult> caseResults = new ArrayList<>();

            for (TestCase testCase : scenario.getTestCases()) {
                long start = System.currentTimeMillis();
                Response response = executeTestCase(testCase);
                long end = System.currentTimeMillis();

                int actualStatus = response.statusCode();
                boolean passed = (actualStatus == testCase.getExpectedStatus());

                caseResults.add(new TestResult(
                        testCase.getName(),
                        testCase.getMethod(),
                        testCase.getEndpoint(),
                        testCase.getExpectedStatus(),
                        actualStatus,
                        end - start,
                        passed,
                        passed ? "PASSED" : "FAILED"
                ));
            }

            // Scenario summary
            long passedCount = caseResults.stream().filter(TestResult::isPassed).count();
            long failedCount = caseResults.size() - passedCount;

            scenarioResults.add(new TestScenarioResult(
                    scenario.getName(),
                    caseResults.size(),
                    (int) passedCount,
                    (int) failedCount,
                    caseResults
            ));

            totalCases += caseResults.size();
            totalPassed += passedCount;
            totalFailed += failedCount;
        }

        return new TestPlanResult(
                plan.getName(),
                plan.getScenarios().size(),
                totalCases,
                totalPassed,
                totalFailed,
                scenarioResults
        );
    }

    // -------------------------------------------------------------------------
    // 🔹 Execute a single test case with RestAssured
    // -------------------------------------------------------------------------
    private Response executeTestCase(TestCase testCase) {
        String method = testCase.getMethod().toUpperCase();

        switch (method) {
            case "POST":
                return RestAssured.given()
                        .contentType("application/json")
                        .body(testCase.getBody())
                        .when()
                        .post(testCase.getEndpoint());
            case "PUT":
                return RestAssured.given()
                        .contentType("application/json")
                        .body(testCase.getBody())
                        .when()
                        .put(testCase.getEndpoint());
            case "DELETE":
                return RestAssured.when().delete(testCase.getEndpoint());
            default:
                return RestAssured.when().get(testCase.getEndpoint());
        }
    }
}
