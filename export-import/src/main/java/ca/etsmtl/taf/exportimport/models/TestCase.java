package ca.etsmtl.taf.exportimport.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Builder;

@Document(collection = "test_cases")
@Builder
@Getter
public class TestCase extends Entity {
    @Id
    private String _id;
    private String testSuiteId;
    private String name;

    @Override
    public EntityType getType() {
        return EntityType.TEST_CASE;
    }


}
