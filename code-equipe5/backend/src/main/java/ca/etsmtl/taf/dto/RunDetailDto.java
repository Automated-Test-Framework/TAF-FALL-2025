package ca.etsmtl.taf.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class RunDetailDto {
    public String runId;
    public String projectKey;
    public String status;
    public Stats stats;
    public Instant createdAt;
    public List<Map<String,Object>> cases; // facultatif (quand on reconstruit depuis test_cases)

    public static class Stats {
        public long total;
        public long passed;
        public long failed;
        public long durationMs;
    }
}
