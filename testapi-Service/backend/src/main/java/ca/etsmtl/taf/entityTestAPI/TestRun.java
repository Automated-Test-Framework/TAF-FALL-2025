package ca.etsmtl.taf.entityTestAPI;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("test_runs")
@CompoundIndex(name="suite_createdAt", def="{ 'suiteId':1, 'createdAt':-1 }")
@Getter
@Setter
public class TestRun {
    @Id
    public String id;
    public String suiteId;
    public String suiteName;
    public String status;      // QUEUED | RUNNING | PASSED | FAILED
    public int found;
    public int passed;
    public int failed;
    public int skipped;
    public Instant createdAt;
    public Instant startedAt;
    public Instant endedAt;
    public String reportIndexPath;      // chemin vers rapport HTML Cucumber
    public List<String> logs;
}
