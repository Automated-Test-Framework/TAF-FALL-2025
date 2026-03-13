package ca.etsmtl.taf.performance.gatling.simulation;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.etsmtl.taf.performance.gatling.model.GatlingTestRequest;
import ca.etsmtl.taf.performance.gatling.model.GatlingTestRequestPercentileResponseTime;
import io.gatling.javaapi.core.Assertion;
import io.gatling.javaapi.core.ChainBuilder;
import io.gatling.javaapi.core.OpenInjectionStep;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

public abstract class BaseSimulation extends Simulation {

    protected String requestJson = System.getProperty("requestJson");
    protected GatlingTestRequest gatlingTestRequest = parseRequestDetails(requestJson);

    private GatlingTestRequest parseRequestDetails(String json) {
        try {
            return new ObjectMapper().readValue(json, GatlingTestRequest.class);
        } catch (Exception e) {
            return null;
        }
    }

    protected HttpProtocolBuilder httpProtocol = http.baseUrl(gatlingTestRequest.getBaseUrl())
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private ChainBuilder createHttpRequest() {
        String methodType = gatlingTestRequest.getMethodType();
        HttpRequestActionBuilder httpRequestBuilder;

        switch (methodType.toUpperCase()) {
            case "GET":    httpRequestBuilder = http(gatlingTestRequest.getRequestName()).get(gatlingTestRequest.getUri()); break;
            case "POST":   httpRequestBuilder = http(gatlingTestRequest.getRequestName()).post(gatlingTestRequest.getUri()).body(StringBody(gatlingTestRequest.getRequestBody())); break;
            case "PUT":    httpRequestBuilder = http(gatlingTestRequest.getRequestName()).put(gatlingTestRequest.getUri()).body(StringBody(gatlingTestRequest.getRequestBody())); break;
            case "DELETE": httpRequestBuilder = http(gatlingTestRequest.getRequestName()).delete(gatlingTestRequest.getUri()); break;
            default: throw new IllegalArgumentException("Invalid HttpRequestMethod: " + methodType);
        }

        return exec(httpRequestBuilder.check(status().not(404), status().not(500)));
    }

    // Méthode abstraite que les enfants doivent implémenter
    protected abstract OpenInjectionStep getInjectionProfile();

    {
        ScenarioBuilder scn = scenario(gatlingTestRequest.getScenarioName()).exec(createHttpRequest());
        
        List<Assertion> assertions = new ArrayList<>();
        if (gatlingTestRequest.getMeanResponseTime() >= 0) {
            assertions.add(global().responseTime().mean().lt(gatlingTestRequest.getMeanResponseTime()));
        }
        if (gatlingTestRequest.getFailedRequestsPercent() >= 0) {
            assertions.add(global().failedRequests().percent().lt(gatlingTestRequest.getFailedRequestsPercent()));
        }
        for (GatlingTestRequestPercentileResponseTime p : gatlingTestRequest.getResponseTimePerPercentile()) {
            assertions.add(global().responseTime().percentile(p.getPercentile()).lt(p.getResponseTime()));
        }

        setUp(scn.injectOpen(getInjectionProfile()))
                .protocols(httpProtocol)
                .assertions(assertions.toArray(new Assertion[0]));
    }
}