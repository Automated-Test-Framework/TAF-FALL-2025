package ca.etsmtl.taf.exportimport.dtos.testrail.outbound;

import ca.etsmtl.taf.exportimport.models.TestCase;

import java.util.HashMap;
import java.util.Map;

public class TestCaseDTO {
    private String id;
    private Integer section_id;
    private String title;

    public TestCaseDTO(TestCase testCase, Integer sectionId) {
        this.id = testCase.get_id();
        this.title = testCase.getName();
        this.section_id = sectionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getSection_id() {
        return section_id;
    }

    public void setSection_id(Integer section_id) {
        this.section_id = section_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("section_id", this.section_id);
        data.put("title", this.title);
        return data;
    }
}
