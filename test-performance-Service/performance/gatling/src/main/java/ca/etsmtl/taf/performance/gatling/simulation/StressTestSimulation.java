package ca.etsmtl.taf.performance.gatling.simulation;

import static io.gatling.javaapi.core.CoreDsl.rampUsersPerSec;

import io.gatling.javaapi.core.OpenInjectionStep;

public class StressTestSimulation extends BaseSimulation {
    @Override
    protected OpenInjectionStep getInjectionProfile() {
        return rampUsersPerSec(gatlingTestRequest.getUserRampUpPerSecondMin())
                .to(gatlingTestRequest.getUserRampUpPerSecondMax())
                .during(java.time.Duration.ofSeconds(gatlingTestRequest.getUserRampUpPerSecondDuration()));
    }
}
