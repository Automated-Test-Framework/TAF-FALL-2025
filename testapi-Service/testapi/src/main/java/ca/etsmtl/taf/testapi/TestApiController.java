package ca.etsmtl.taf.testapi;

import ca.etsmtl.taf.testapi.payload.request.TestApiRequest;
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
        return new TestRequestExecutor(request).getAnswer();
    }
}
