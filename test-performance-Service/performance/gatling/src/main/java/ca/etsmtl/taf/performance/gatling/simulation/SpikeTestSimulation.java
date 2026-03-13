package ca.etsmtl.taf.performance.gatling.simulation;

import ca.etsmtl.taf.performance.gatling.model.GatlingTestRequest;
import ca.etsmtl.taf.performance.gatling.model.GatlingTestRequestPercentileResponseTime;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class SpikeTestSimulation extends Simulation {

    private String requestJson = System.getProperty("requestJson");


    private GatlingTestRequest gatlingTestRequest = parseRequestDetails(requestJson);


    private GatlingTestRequest parseRequestDetails(String requestJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(requestJson, GatlingTestRequest.class);
        } catch (Exception e) {
            return null;
        }
    }
    private HttpProtocolBuilder httpProtocol;
    private ScenarioBuilder scn;

    private static String ensureProtocol(String url) {
        if (url != null && !url.startsWith("http://") && !url.startsWith("https://")) {
            return "http://" + url;
        }
        return url;
    }

    private ChainBuilder createHttpRequest() {
        String methodType = gatlingTestRequest.getMethodType();
        HttpRequestActionBuilder httpRequestBuilder;

        switch (methodType.toUpperCase()) {
            case "GET":
                httpRequestBuilder =http(gatlingTestRequest.getRequestName())
                        .get(gatlingTestRequest.getUri());
                break;
            case "POST":
                httpRequestBuilder = http(gatlingTestRequest.getRequestName())
                        .post(gatlingTestRequest.getUri())
                        .body(StringBody(gatlingTestRequest.getRequestBody()));
                break;
            case "PUT":
                httpRequestBuilder = http(gatlingTestRequest.getRequestName())
                        .put(gatlingTestRequest.getUri())
                        .body(StringBody(gatlingTestRequest.getRequestBody()));
                break;
            case "DELETE":
                httpRequestBuilder = http(gatlingTestRequest.getRequestName())
                        .delete(gatlingTestRequest.getUri());
                break;
            default:
                throw new IllegalArgumentException("Invalid HttpRequestMethod: " + methodType.toUpperCase());
        }

        return exec(httpRequestBuilder
                .check(status().not(404), status().not(500)));
    }

    private ScenarioBuilder createScenario() {
        return scenario(gatlingTestRequest.getScenarioName())
                .exec(createHttpRequest());
    }

    {
        if (gatlingTestRequest == null) {
            throw new RuntimeException("Could not parse GatlingTestRequest from property requestJson: " + requestJson);
        }

        this.httpProtocol = http.baseUrl(ensureProtocol(gatlingTestRequest.getBaseUrl()))
                .acceptHeader("application/json")
                .contentTypeHeader("application/json");

        this.scn = createScenario();

        List<Assertion> assertions = new ArrayList<>();

        if (gatlingTestRequest.getMeanResponseTime() >= 0) {
            assertions.add(global().responseTime().mean().lt(gatlingTestRequest.getMeanResponseTime()));
        }

        if (gatlingTestRequest.getFailedRequestsPercent() >= 0) {
            assertions.add(global().failedRequests().percent().lt(gatlingTestRequest.getFailedRequestsPercent()));
        }

        if (gatlingTestRequest.getResponseTimePerPercentile() != null && gatlingTestRequest.getResponseTimePerPercentile().size() > 0) {
            for(GatlingTestRequestPercentileResponseTime assertion : gatlingTestRequest.getResponseTimePerPercentile()){
                assertions.add(global().responseTime().percentile(assertion.getPercentile()).lt(assertion.getResponseTime()));
            }
        }

        setUp(
                scn.injectOpen(
                        constantUsersPerSec(gatlingTestRequest.getConstantUsers()).during(Duration.ofSeconds(gatlingTestRequest.getConstantUsersDuration())),
                        atOnceUsers(gatlingTestRequest.getUsersAtOnce()),
                        constantUsersPerSec(gatlingTestRequest.getUsersAtOnce()).during(Duration.ofMinutes(3)),
                        constantUsersPerSec(gatlingTestRequest.getConstantUsers()).during(Duration.ofSeconds(gatlingTestRequest.getConstantUsersDuration()))
                )
        ).protocols(httpProtocol)
                .assertions(assertions.toArray(new Assertion[0]));
    }
}
