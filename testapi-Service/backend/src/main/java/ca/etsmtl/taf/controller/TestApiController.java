package ca.etsmtl.taf.controller;

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
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/testapi")
@SecurityRequirement(name = "bearerAuth")
public class TestApiController {
    @Value("${taf.app.testAPI_url}")
    String Test_API_microservice_url;

    @Value("${taf.app.testAPI_port}")
    String Test_API_microservice_port;

    @Value("${taf.app.testAPI_timeout:30000}")
    int testApiTimeout;

    @PostMapping("/checkApi")
    public ResponseEntity<String> testApi(@Valid @RequestBody TestApiRequest testApiRequest) throws URISyntaxException, IOException, InterruptedException {
        var uri = new URI(Test_API_microservice_url+":"+Test_API_microservice_port+"/microservice/testapi/checkApi");
        uri.toString().trim();
        ObjectMapper objectMapper = new ObjectMapper();

        String requestBody = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(testApiRequest);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(testApiTimeout))
                .build();

        HttpRequest request = HttpRequest.newBuilder(uri)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMillis(testApiTimeout))
                .POST(BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response =
                client.send(request, BodyHandlers.ofString());
        return ResponseEntity.ok(response.body());
    }
}
