package ca.etsmtl.taf.exportimport.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/fake")
public class FakeController {

    @GetMapping("/endpoint")
    public ResponseEntity<?> fakeEndpoint() {
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "very nice !"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", "how ?"
            ));
        }
    }
}

