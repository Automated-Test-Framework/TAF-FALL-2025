package ca.etsmtl.taf.exportimport.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import lombok.Getter;
import lombok.Builder;

@Document(collection = "test_runs")
@Builder
@Getter
public class TestRun extends Entity {
    @Id
    private String _id;
    private String testSuiteId;
    private String name;
    private List<String> testCaseIds;

    @Override
    public EntityType getType() {
        return EntityType.TEST_RUN;
    }
}
