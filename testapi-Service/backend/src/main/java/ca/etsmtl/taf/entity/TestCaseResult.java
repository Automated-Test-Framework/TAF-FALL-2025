package ca.etsmtl.taf.entity;

import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

/**
 * Individual test case result — used by Dashboard (Équipe 6) for graphs/filters.
 * Schema matches: test_cases collection.
 */
@Getter
@Setter
@Document(collection = "test_cases")
public class TestCaseResult {

    @Id
    private String id;

    private String project;
    private String runId;
    private String name;
    private String suite;
    private String type;       // "UI" | "API"
    private String tool;       // "Selenium" | "RestAssured" | "JMeter"
    private String status;     // "passed" | "failed"
    private long durationMs;
    private Date executedAt;
    private String executedBy;
    private List<String> requirements;
}
