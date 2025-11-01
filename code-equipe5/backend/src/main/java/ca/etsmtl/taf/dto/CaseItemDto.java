package ca.etsmtl.taf.dto;

import java.time.Instant;
import java.util.Map;

public class CaseItemDto {
    public String runId;
    public String project;
    public String suite;
    public String type;
    public String tool;
    public String name;
    public String status;
    public Instant executedAt;
    public Long durationMs;
    public Map<String,Object> metrics;
    public Map<String,Object> links;
}
