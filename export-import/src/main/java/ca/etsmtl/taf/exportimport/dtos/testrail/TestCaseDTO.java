package ca.etsmtl.taf.exportimport.dtos.testrail;

import ca.etsmtl.taf.exportimport.models.TestCase;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestCaseDTO {
    private String id;
    private Integer section_id;
    private String title;

    public TestCaseDTO(TestCase testCase, Integer sectionId) {
        this.id = testCase.get_id();
        this.title = testCase.getName();
        this.section_id = sectionId;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("section_id", this.section_id);
        data.put("title", this.title);
        return data;
    }
}
