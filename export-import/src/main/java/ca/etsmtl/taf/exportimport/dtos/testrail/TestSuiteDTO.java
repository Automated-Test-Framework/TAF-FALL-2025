package ca.etsmtl.taf.exportimport.dtos.testrail;

import ca.etsmtl.taf.exportimport.models.TestSuite;

import java.util.HashMap;
import java.util.Map;

public class TestSuiteDTO {
    private String name;
    private String description;

    public TestSuiteDTO() {

    }

    public TestSuiteDTO(TestSuite testSuite) {
        this.name = testSuite.getName();
        this.description = testSuite.getDescription();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
