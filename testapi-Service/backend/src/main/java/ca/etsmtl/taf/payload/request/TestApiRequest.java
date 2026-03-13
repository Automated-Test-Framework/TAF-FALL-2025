package ca.etsmtl.taf.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public class TestApiRequest {
    @NotBlank
    @Schema(example = "GET")
    private String method;

    @NotBlank
    @Schema(example = "https://jsonplaceholder.typicode.com/posts/1")
    private String apiUrl;

    @Schema(example = "200")
    private int statusCode;

    @Schema(example = "")
    private String input;

    @Schema(example = "")
    private String expectedOutput;

    @Schema(example = "{}")
    private Map<String, String> headers;

    private int responseTime;

    @Schema(example = "{}")
    private Map<String, String> expectedHeaders;

    // Getters et setters
    public String getMethod() { return this.method; }
    public void setMethod(String method) { this.method = method; }

    public String getApiUrl() { return this.apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public int getStatusCode() { return this.statusCode; }

    public String getInput() { return this.input; }
    public void setInput(String input) { this.input = input; }

    public String getExpectedOutput() { return this.expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public int getResponseTime() { return responseTime; }
    public void setResponseTime(int responseTime) { this.responseTime = responseTime; }

    public Map<String, String> getExpectedHeaders() { return expectedHeaders; }
    public void setExpectedHeaders(Map<String, String> expectedHeaders) { this.expectedHeaders = expectedHeaders; }
}