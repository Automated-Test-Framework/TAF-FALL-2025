package ca.etsmtl.taf.entity;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

/**
 * Summary document per test run — used by Dashboard (Équipe 6) for KPIs.
 * Schema matches: test_runs collection.
 */
@Getter
@Setter
@Document(collection = "test_runs")
public class TestRun {

    @Id
    private String id;

    private Project project;
    private Pipeline pipeline;
    private Run run;
    private Date createdAt;

    @Getter @Setter
    public static class Project {
        private String key;
    }

    @Getter @Setter
    public static class Pipeline {
        private String runId;
    }

    @Getter @Setter
    public static class Run {
        private String status; // "passed" | "failed"
        private Stats stats;
    }

    @Getter @Setter
    public static class Stats {
        private int total;
        private int passed;
        private int failed;
        private long durationMs;
    }
}
