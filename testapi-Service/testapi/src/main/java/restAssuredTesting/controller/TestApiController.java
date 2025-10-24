package restAssuredTesting.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import restAssuredTesting.requests.payload.request.TestApiRequest;
import restAssuredTesting.service.TestRunnerService;

import java.io.Serializable;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/microservice/testapi")
public class TestApiController {

    @Autowired
    TestRunnerService testRunnerService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK"));
    }

    @GetMapping("/slow")
    public ResponseEntity<String> slowTest() {
        try {
            String result = testRunnerService.testSlowEndpoint();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Internal Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }


    @PostMapping("/checkApi")
    public Serializable testApi(@RequestBody TestApiRequest testApiRequest) {
        return (redirectMethod(testApiRequest));
    }

    public Serializable redirectMethod(TestApiRequest request) {
        return new RequestController(request).getAnswer();
    }
}
