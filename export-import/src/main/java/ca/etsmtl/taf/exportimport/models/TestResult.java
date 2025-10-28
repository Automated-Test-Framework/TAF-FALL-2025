package ca.etsmtl.taf.exportimport.models;

import ca.etsmtl.taf.exportimport.models.TestRunStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Builder;

@Document(collection = "test_results")
@Builder
@Getter
public class TestResult extends Entity {
    @Id
    private String _id;
    private String testRunId;
    private String testCaseId;
    private TestRunStatus status;

    @Override
    public EntityType getType() {
        return EntityType.TEST_RESULT;
    }
}
