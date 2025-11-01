package ca.etsmtl.taf.dto;


import java.time.Instant;

public class RunCardDto {
    public String projectKey;
    public String runId;
    public String status;
    public long total;
    public long passed;
    public long failed;
    public Instant createdAt;
}
