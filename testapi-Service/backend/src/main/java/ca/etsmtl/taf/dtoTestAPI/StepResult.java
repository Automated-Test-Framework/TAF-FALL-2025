package ca.etsmtl.taf.dtoTestAPI;

public class StepResult {

    private String name;
    private int statusCode;
    private boolean passed;
    private String error;
    private long durationMs;

    public StepResult() {}
    public StepResult(String name, int statusCode, boolean passed, String error, long durationMs) {
        this.name = name; this.statusCode = statusCode; this.passed = passed; this.error = error; this.durationMs = durationMs;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }
}
