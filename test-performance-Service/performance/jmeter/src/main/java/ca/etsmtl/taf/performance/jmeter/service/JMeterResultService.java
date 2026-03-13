package ca.etsmtl.taf.performance.jmeter.service;

import ca.etsmtl.taf.performance.jmeter.model.JMeterResponse;
import ca.etsmtl.taf.performance.jmeter.model.JMeterTestDocument;
import ca.etsmtl.taf.performance.jmeter.model.TestPlanBase;
import ca.etsmtl.taf.performance.jmeter.repository.JMeterTestsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JMeterResultService {

    private static final Logger logger = LoggerFactory.getLogger(JMeterResultService.class);

    @Autowired
    private JMeterTestsRepository jMeterRepository;

    public void saveTestResults(TestPlanBase testPlanRequest, JMeterResponse testPlanResponse) {
        JMeterTestDocument results = JMeterTestDocument.builder()
                .testRequest(testPlanRequest)
                .testResult(testPlanResponse)
                .build();
        try {
            jMeterRepository.save(results);
        } catch (Exception e) {
            logger.error("Mongo error: ", e);
        }
    }
}
