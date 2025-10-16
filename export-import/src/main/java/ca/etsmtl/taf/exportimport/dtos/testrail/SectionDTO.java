package ca.etsmtl.taf.exportimport.dtos.testrail;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SectionDTO {

    private Integer testSuiteId;
    private String name;

    public SectionDTO(Integer testSuiteIdTR, String name) {
        this.testSuiteId = testSuiteIdTR;
        this.name = name;
    }

    /**
     * Convertit ce DTO en un format JSON-compatible pour l’API TestRail.
     */
    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("suite_id", this.testSuiteId);
        data.put("name", this.name);
        return data;
    }

}
