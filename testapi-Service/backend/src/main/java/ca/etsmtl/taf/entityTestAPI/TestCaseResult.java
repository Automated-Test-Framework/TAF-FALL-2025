package ca.etsmtl.taf.entityTestAPI;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("test_case_results")
@CompoundIndex(name="run_name", def="{ 'runId':1, 'name':1 }")
@Getter
@Setter
public class TestCaseResult {

    @Id
    public String id;
    public String runId;
    public String name;
    public int statusCode;
    public boolean passed;
    public String error;
    public long durationMs;

}
