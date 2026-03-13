package ca.etsmtl.taf.performance.jmeter.service;

import ca.etsmtl.taf.performance.jmeter.model.*;
import ca.etsmtl.taf.performance.jmeter.repository.JMeterTestsRepository;
import ca.etsmtl.taf.performance.jmeter.utils.JMeterRunner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JMeterService {
    private final JMeterTestsRepository jMeterTestsRepository;

    public JMeterService(JMeterTestsRepository jMeterTestsRepository) {
        this.jMeterTestsRepository = jMeterTestsRepository;
    }

    public JMeterResponse runTest(TestPlanBase testPlan) throws Exception {

        try {
            JMeterResponse response = JMeterRunner.executeTestPlanAndGenerateReport(testPlan);
            response.setStatus("success");
            response.setMessage("Test plan executed successfully");
            saveTestDocument(testPlan, response);
            return response;
        } catch (Exception e) {
            log.error("Error executing JMeter test: {}", e.getMessage());
            throw e; // On laisse le contrôleur ou un GlobalExceptionHandler gérer le statut HTTP....
        }
    }

    private void saveTestDocument(TestPlanBase testPlan, JMeterResponse testPlanResponse) {
        try {
            JMeterTestDocument testDocument = JMeterTestDocument.builder()
                    .testRequest(testPlan)
                    .testResult(testPlanResponse)
                    .build();
            jMeterTestsRepository.save(testDocument);
        } catch (Exception e) {
            log.error("MongoDB persistence error: {}", e.getMessage());
        }
    }
}
