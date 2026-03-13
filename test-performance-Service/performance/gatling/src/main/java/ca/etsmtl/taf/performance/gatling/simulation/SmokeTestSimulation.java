package ca.etsmtl.taf.performance.gatling.simulation;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;

import io.gatling.javaapi.core.OpenInjectionStep;

public class SmokeTestSimulation extends BaseSimulation {
    @Override
    protected OpenInjectionStep getInjectionProfile() {
        return atOnceUsers(gatlingTestRequest.getUsersAtOnce());
    }
}
