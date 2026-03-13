package ca.etsmtl.taf.performance.jmeter;

import ca.etsmtl.taf.performance.jmeter.model.*;
import ca.etsmtl.taf.performance.jmeter.repository.JMeterTestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ca.etsmtl.taf.performance.jmeter.model.*;
import ca.etsmtl.taf.performance.jmeter.utils.JMeterRunner;
import ca.etsmtl.taf.performance.jmeter.service.JMeterService;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/performance/jmeter")
public class JMeterController {

  private final JMeterService jMeterService;

  public JMeterController(JMeterService jMeterService) {
    this.jMeterService = jMeterService;
  }

  @PostMapping("/http")
  public ResponseEntity<JMeterResponse> executeHttpTest(@RequestBody HttpTestPlan testPlan) {
    // Applique les valeurs par défaut
    sanitizeHttpPlan(testPlan);
    return processRequest(testPlan);
  }

  @PostMapping("/ftp")
  public ResponseEntity<JMeterResponse> executeFtpTest(@RequestBody FTPTestPlan testPlan) {
    return processRequest(testPlan);
  }

  private ResponseEntity<JMeterResponse> processRequest(TestPlanBase testPlan) {
    try {
      JMeterResponse response = jMeterService.runTest(testPlan);
      return ResponseEntity.ok(response);
    } catch (JMeterRunnerException e) {
      return ResponseEntity.badRequest().body(new JMeterResponse("failure", e.getMessage(), null, null));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(new JMeterResponse("failure", "Internal error: " + e.getMessage(), null, null));
    }
  }

  private void sanitizeHttpPlan(HttpTestPlan plan) {
    if (plan.getProtocol() == null)
      plan.setProtocol("http");
    if (plan.getPort() == null)
      plan.setPort("");
    if (plan.getDuration() == null)
      plan.setDuration("");
    if (plan.getData() == null)
      plan.setData("");
  }

}
