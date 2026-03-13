package ca.etsmtl.taf.testapi;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Test-only controller that simulates a slow API endpoint for timeout testing.
 * Only loaded in non-production profiles to prevent DoS abuse.
 */
@Profile({"dev", "local", "default"})
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/microservice/testapi")
public class SlowEndpointController {

    private static final int MAX_DELAY_MS = 120_000;

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
