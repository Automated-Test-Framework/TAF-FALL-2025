package ca.etsmtl.taf.performance.gatling.simulation;

public class SimulationFactory {
    public String getSimulationName(String simulationType) {
        switch (simulationType) {
            case "DEFAULT":
                return DefaultSimulation.class.getName();
            case "LOAD_TEST":
                return LoadTestSimulation.class.getName();
            case "SMOKE_TEST":
                return SmokeTestSimulation.class.getName();
            case "SPIKE_TEST":
                return SpikeTestSimulation.class.getName();
            case "STRESS_TEST":
                return StressTestSimulation.class.getName();
            default:
                throw new IllegalArgumentException("Unknown simulation strategy type: " + simulationType);
        }
    }
}
