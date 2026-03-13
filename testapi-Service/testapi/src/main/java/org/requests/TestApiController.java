package org.requests;

import org.requests.payload.request.TestApiRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/microservice/testapi")
public class TestApiController {
    @PostMapping("/checkApi")
    public Serializable testApi(@Valid @RequestBody TestApiRequest testApiRequest) {
        return (redirectMethod(testApiRequest));
    }

    public Serializable redirectMethod(TestApiRequest request) {
        return new RequestController(request).getAnswer();
    }

    private static final int MAX_DELAY_MS = 120_000;

    /**
     * Simulates a slow API endpoint to test timeout behavior.
     * Sleeps for the given number of milliseconds (default 15000, max 120000).
     */
    @GetMapping("/slow")
    public ResponseEntity<Map<String, Object>> slow(
            @RequestParam(value = "delay", defaultValue = "15000") int delay) {
        if (delay < 0 || delay > MAX_DELAY_MS) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "delay must be between 0 and " + MAX_DELAY_MS + " ms"));
        }
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long elapsed = System.currentTimeMillis() - start;
        return ResponseEntity.ok(Map.of(
                "message", "Slow response after " + elapsed + " ms",
                "delay", delay,
                "elapsed", elapsed));
    }
}
