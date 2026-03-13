package ca.etsmtl.taf.performance.gatling.simulation;

import static io.gatling.javaapi.core.CoreDsl.rampUsers;

import java.time.Duration;

import io.gatling.javaapi.core.OpenInjectionStep;

public class LoadTestSimulation extends BaseSimulation {
    @Override
    protected OpenInjectionStep getInjectionProfile() {
        return rampUsers(gatlingTestRequest.getUsersNumber())
                .during(Duration.ofSeconds(gatlingTestRequest.getRampUpDuration()));
    }
}
