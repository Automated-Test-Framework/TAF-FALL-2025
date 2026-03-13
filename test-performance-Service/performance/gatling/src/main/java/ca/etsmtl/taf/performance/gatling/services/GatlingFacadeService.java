package ca.etsmtl.taf.performance.gatling.services;

import ca.etsmtl.taf.performance.gatling.model.GatlingTestRequest;
import ca.etsmtl.taf.performance.gatling.model.GatlingTestResponse;
import ca.etsmtl.taf.performance.gatling.model.GatlingTestResult;
import ca.etsmtl.taf.performance.gatling.model.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Primary
@Service
public class GatlingFacadeService implements GatlingFacade {
    @Autowired
    private SimulationService simulationService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private ResultService resultService;

    @Override
    public MessageResponse runSimulation(GatlingTestRequest gatlingRequest) {
        try {
            String output = simulationService.runSimulation(gatlingRequest);

            GatlingTestResponse testResponse = GatlingTestResponse.builder()
                    .response(output)
                    .build();

            resultService.saveTestResults(gatlingRequest, testResponse);

            GatlingTestResult results = reportService.getLatestReportResult();
            return new MessageResponse("Simulation finished. Output:\n" + output, results);
        } catch (Exception e) {
            return new MessageResponse("Error executing simulation: " + e.getMessage(), null);
        }
    }

    @Override
    public String getLatestReportPath() {
        return reportService.getLatestReportPath();
    }
}
