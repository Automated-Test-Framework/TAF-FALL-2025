package ca.etsmtl.taf.exportimport.dtos.testrail;

import ca.etsmtl.taf.exportimport.models.TestSuite;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestSuiteDTO {
    private String id;
    private String name;
    private String description;

    public TestSuiteDTO(TestSuite testSuite) {
        this.id = testSuite.get_id();
        this.name = testSuite.getName();
        this.description = testSuite.getDescription();
    }

    /**
     * Convertit ce DTO en un format JSON-compatible pour l’API TestRail.
     */
    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("description", this.description);
        return data;
    }
}
