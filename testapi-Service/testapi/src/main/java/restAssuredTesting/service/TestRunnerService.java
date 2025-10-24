package restAssuredTesting.service;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;
import restAssuredTesting.model.*;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TestRunnerService {

    // -------------------------------------------------------------------------
    // Global configuration block (executed once)
    // -------------------------------------------------------------------------
    static {
        RestAssured.baseURI = "http://localhost:8082";

        RestAssured.config = RestAssuredConfig.config()
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.connection.timeout", 10000)
                        .setParam("http.socket.timeout", 10000)
                        .setParam("http.connection-manager.timeout", 10000L));
    }
    // -------------------------------------------------------------------------
    // Run a full test plan (scenarios + cases)
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
    // Execute a single test case with RestAssured
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

    // -------------------------------------------------------------------------
    // NEW: Method to explicitly test the slow endpoint and verify timeout
    // -------------------------------------------------------------------------
    public String testSlowEndpoint() {
        long start = System.currentTimeMillis();
        try {
            log.info("Calling /microservice/testapi/slow (expected 15s delay, 10s timeout)...");
            RestAssured.given()
                    .config(RestAssured.config)
                    .when()
                    .get("/microservice/testapi/slow")
                    .then()
                    .statusCode(200); // Won't reach if timeout happens
            long duration = System.currentTimeMillis() - start;
            return "Completed successfully in " + duration + " ms (no timeout)";
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            if (e.getCause() instanceof SocketTimeoutException || e instanceof SocketTimeoutException) {
                return "Timeout triggered after " + duration + " ms (" + e.getClass().getSimpleName() + ")";
            }
            return "Unexpected error after " + duration + " ms: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }
}
