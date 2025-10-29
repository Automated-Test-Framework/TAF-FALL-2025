package ca.etsmtl.taf.controller;

import ca.etsmtl.taf.entityTestAPI.TestRun;
import ca.etsmtl.taf.repositoryTestAPI.TestCaseResultRepo;
import ca.etsmtl.taf.repositoryTestAPI.TestRunRepo;
import ca.etsmtl.taf.repositoryTestAPI.TestSuiteRepo;
import ca.etsmtl.taf.serviceTestAPI.QaOrchestrator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ca.etsmtl.taf.payload.request.TestApiRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.http.*;

import org.springframework.beans.factory.annotation.Value;



@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/testapi")
public class TestApiController {

    private final QaOrchestrator orchestrator;
    private final TestRunRepo runRepo;
    private final TestCaseResultRepo caseRepo;
    private final TestSuiteRepo suiteRepo;

    public TestApiController(QaOrchestrator orchestrator, TestRunRepo runRepo, TestCaseResultRepo caseRepo, TestSuiteRepo suiteRepo) {
        this.orchestrator = orchestrator;
        this.runRepo = runRepo;
        this.caseRepo = caseRepo;
        this.suiteRepo = suiteRepo;
    }

    @Value("${taf.app.testAPI_url}")
    String Test_API_microservice_url;

    @Value("${taf.app.testAPI_port}")
    String Test_API_microservice_port;

    @PostMapping("/checkApi")
    public ResponseEntity<String> testApi(@Valid @RequestBody TestApiRequest testApiRequest) throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI(Test_API_microservice_url+":"+Test_API_microservice_port+"/microservice/testapi/checkApi");
        uri.toString().trim();
        ObjectMapper objectMapper = new ObjectMapper();

        String requestBody = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(testApiRequest);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                client.send(request, BodyHandlers.ofString());
        return ResponseEntity.ok(response.body());
    }

    @PostMapping("/suites")
    public Map<String,String> createSuite(@RequestBody CreateSuiteReq req){
        String id = orchestrator.saveSuite(req.name, req.owner, req.format, req.content);
        return Map.of("suiteId", id);
    }

    @GetMapping("/suites")
    public ResponseEntity<?> getAllSuite(){
        return ResponseEntity.ok(suiteRepo.findAll());
    }

    @DeleteMapping("/suites")
    public void deleteSuite(@RequestParam("suiteId") String suiteId){
         suiteRepo.deleteById(suiteId);
        return;
    }

    @PostMapping("/runs")
    public Map<String,String> createRun(@RequestBody CreateRunReq req){
        String runId = orchestrator.submitRun(req.suiteId, req.overrideYaml);
        return Map.of("runId", runId);
    }

    @GetMapping("/runs")
    public ResponseEntity<TestRun> getRun( @RequestParam("runId") String runId){
        return runRepo.findById(runId).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/runs/cases")
    public ResponseEntity<?> getCases(@RequestParam("runId") String runId){
        return ResponseEntity.ok(caseRepo.findAll().stream().filter(c -> runId.equals(c.runId)).toList());
    }

    @GetMapping("/runs/report")
    public ResponseEntity<byte[]> report(@RequestParam("runId") String runId) throws IOException {
        var run = runRepo.findById(runId).orElse(null);
        if (run==null || run.reportIndexPath==null) return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        Path p = Path.of(run.reportIndexPath);
        if (!Files.exists(p)) return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(Files.readAllBytes(p));
    }
}
