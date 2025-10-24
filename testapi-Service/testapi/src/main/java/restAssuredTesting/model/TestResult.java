package restAssuredTesting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {
    private String testCaseName;
    private String method;
    private String endpoint;
    private int expectedStatus;
    private int actualStatus;
    private long responseTimeMs;
    private boolean passed;
    private String message;
}

