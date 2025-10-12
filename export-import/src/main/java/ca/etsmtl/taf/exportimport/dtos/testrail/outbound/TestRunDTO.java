package ca.etsmtl.taf.exportimport.dtos.testrail.outbound;

import ca.etsmtl.taf.exportimport.models.TestRun;

import java.util.HashMap;
import java.util.Map;

public class TestRunDTO {
    private String id;
    private Integer testSuiteId;
    private String name;

    public TestRunDTO(TestRun testRun, Integer testSuiteId) {
        this.id = testRun.get_id();
        this.testSuiteId = testSuiteId;
        this.name = testRun.getName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTestSuiteId() {
        return testSuiteId;
    }

    public void setTestSuiteId(Integer testSuiteId) {
        this.testSuiteId = testSuiteId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("suite_id", this.testSuiteId);
        data.put("name", this.name);
        return data;
    }
}
