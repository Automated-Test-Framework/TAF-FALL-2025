package ca.etsmtl.taf.performance.gatling.controllers;


import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.etsmtl.taf.performance.gatling.model.MessageResponse;
import ca.etsmtl.taf.performance.gatling.model.GatlingTestRequest;
import ca.etsmtl.taf.performance.gatling.services.GatlingFacade;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/performance/gatling")
public class GatlingApiController {

    @Autowired
    GatlingFacade gatlingFacade;

    @PostMapping(value = "/runSimulation")
    public ResponseEntity<MessageResponse> runSimulation(@RequestBody GatlingTestRequest gatlingRequest) {
        MessageResponse response = gatlingFacade.runSimulation(gatlingRequest);
        if (response.getMessage() != null && response.getMessage().startsWith("Error")) {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/latest-report")
    public ResponseEntity<?> getLatestGatlingReport() {
        try {
            String reportPath = gatlingFacade.getLatestReportPath();
            org.slf4j.LoggerFactory.getLogger(GatlingApiController.class)
                .info("Redirecting browser to report at: {}", reportPath);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(reportPath))
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur de lecture du fichier de rapport.");
        }
    }

}
