package ca.etsmtl.taf.exportimport.dtos.testrail;

import ca.etsmtl.taf.exportimport.models.TestRun;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestRunDTO {
    private String id;
    private Integer testSuiteId;
    private String name;

    public TestRunDTO(TestRun testRun, Integer testSuiteId) {
        this.id = testRun.get_id();
        this.testSuiteId = testSuiteId;
        this.name = testRun.getName();
    }

    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("suite_id", this.testSuiteId);
        data.put("name", this.name);
        return data;
    }
}
