package ca.etsmtl.taf.exportimport.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Builder;

@Document(collection = "test_suites")
@Builder
@Getter
public class TestSuite extends Entity {
    @Id
    private String _id;
    private String projectId;
    private String name;
    private String description;

    @Override
    public EntityType getType() {
        return EntityType.TEST_SUITE;
    }
}
