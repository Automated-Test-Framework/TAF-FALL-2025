package ca.etsmtl.taf.controller;

import ca.etsmtl.taf.entity.GatlingRequest;
import ca.etsmtl.taf.service.GatlingExecutionService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/gatling")
@SecurityRequirement(name = "bearerAuth")
public class GatlingApiController {

    private final GatlingExecutionService gatlingExecutionService;

    public GatlingApiController(GatlingExecutionService gatlingExecutionService) {
        this.gatlingExecutionService = gatlingExecutionService;
    }

    @PostMapping("/runSimulation")
    public String runSimulation(@RequestBody GatlingRequest gatlingRequest) {
        try {
            return gatlingExecutionService.runSimulation(gatlingRequest);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        } catch (URISyntaxException e) {
            return "Error: " + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Error: " + e.getMessage();
        }
    }
}
